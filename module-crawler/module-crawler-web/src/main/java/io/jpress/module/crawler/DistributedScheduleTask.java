package io.jpress.module.crawler;

import com.jfinal.log.Log;
import io.jboot.Jboot;
import io.jboot.support.redis.JbootRedis;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

/**
 * 分布式定时任务
 *
 * @author Eric.Huang
 * @date 2019-05-20 11:45
 * @package io.jpress.module.crawler
 **/

public class DistributedScheduleTask extends Task {

    private static final Log LOG = Log.getLog(DistributedScheduleTask.class);
    private JbootRedis redis;
    private int expire = 50 * 1000;
    private String key;
    private Task task;
    private Runnable runnable;

    public DistributedScheduleTask(){
        this.redis = Jboot.getRedis();
        this.key = "jbootTask:" + this.getClass().getName();

        if (redis == null) {
            LOG.warn("redis is null, " +
                    "can not use DistributedScheduleTask in your Class[" + this.getClass().getName() + "], " +
                    "or config redis info in jboot.properties");
        }
    }

    public DistributedScheduleTask(Task task){
        this.task = task;
        this.redis = Jboot.getRedis();
        this.key = "jbootTask:" + task.getClass().getName();

        if (redis == null) {
            LOG.warn("redis is null, " +
                    "can not use DistributedScheduleTask in your Class[" + runnable.getClass().getName() + "], " +
                    "or config redis info in jboot.properties");
        }
    }

    public DistributedScheduleTask(Task task, int expire){
        this.expire = (expire - 1) * 1000;
        this.task = task;
        this.redis = Jboot.getRedis();
        this.key = "jbootTask:" + task.getClass().getName();

        if (redis == null) {
            LOG.warn("redis is null, " +
                    "can not use DistributedScheduleTask in your Class[" + runnable.getClass().getName() + "], " +
                    "or config redis info in jboot.properties");
        }
    }

    public DistributedScheduleTask(Runnable runnable){
        this.runnable = runnable;
        this.redis = Jboot.getRedis();
        this.key = "jbootTask:" + runnable.getClass().getName();

        if (redis == null) {
            LOG.warn("redis is null, " +
                    "can not use DistributedScheduleTask in your Class[" + runnable.getClass().getName() + "], " +
                    "or config redis info in jboot.properties");
        }
    }

    public DistributedScheduleTask(Runnable runnable,int expire){
        this.expire = (expire - 1) * 1000;
        this.runnable = runnable;
        this.redis = Jboot.getRedis();
        this.key = "jbootTask:" + runnable.getClass().getName();

        if (redis == null) {
            LOG.warn("redis is null, " +
                    "can not use DistributedScheduleTask in your Class[" + runnable.getClass().getName() + "], " +
                    "or config redis info in jboot.properties");
        }
    }
    @Override
    public void execute(TaskExecutionContext context) throws RuntimeException {
        if (redis == null) {
            return;
        }

        Long result = null;

        for (int i = 0; i < 5; i++) {

            Long setTimeMillis = System.currentTimeMillis();
            result = redis.setnx(key, setTimeMillis);

            //error
            if (result == null) {
                quietSleep();
            }

            //setnx fail
            else if (result == 0) {
                Long saveTimeMillis = redis.get(key);
                if (saveTimeMillis == null) {
                    reset();
                }
                long ttl = System.currentTimeMillis() - saveTimeMillis;
                if (ttl > expire) {
                    //防止死锁
                    reset();
                }

                // 休息 2 秒钟，重新去抢，因为可能别的应用执行失败了
                quietSleep();

            }

            //set success
            else if (result == 1) {
                break;
            }
        }

        //抢了5次都抢不到，证明已经被别的应用抢走了
        if (result == null || result == 0) {
            return;
        }

        try {

            if (task != null) {
                task.execute(context);
            }

            if(runnable !=null){
                runnable.run();
            }else{
                boolean runSuccess = execute();

                //run()执行失败，让别的分布式应用APP去执行
                //如果run()执行的时间很长（超过30秒）,那么别的分布式应用可能也抢不到了，只能等待下次轮休
                //作用：故障转移
                if (!runSuccess) {
                    reset();
                }
            }
        }

        // 如果 run() 执行异常，让别的分布式应用APP去执行
        // 作用：故障转移
        catch (Throwable ex) {
            LOG.error(ex.toString(), ex);
            reset();
        }
    }

    /**
     * 重置分布式的key
     */
    private void reset() {
        redis.del(key);
    }

    public void quietSleep() {

        int millis = 2000;
        if (this.expire <= 2000) {
            millis = 100;
        } else if (this.expire <= 5000) {
            millis = 500;
        } else if (this.expire <= 300000) {
            millis = 1000;
        }

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean execute() {
        return true;
    }

}
