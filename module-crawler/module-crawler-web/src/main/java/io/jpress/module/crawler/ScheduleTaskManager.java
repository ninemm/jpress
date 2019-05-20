package io.jpress.module.crawler;

import com.jfinal.aop.Aop;
import com.jfinal.log.Log;
import io.jboot.components.schedule.JbootDistributedRunnable;
import io.jboot.utils.ClassUtil;
import io.jpress.module.crawler.model.ScheduleTask;
import io.jpress.module.crawler.service.ScheduleTaskService;
import it.sauronsoftware.cron4j.Scheduler;
import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于Cron4j的任务调度管理，支持在线添加，删除，修改任务执行时间
 *
 * @author Eric.Huang
 * @date 2019-05-20 11:41
 * @package io.jpress.module.crawler
 **/

public class ScheduleTaskManager {

    private static final Log LOG = Log.getLog(ScheduleTaskManager.class);

    private static final ScheduleTaskManager manager = new ScheduleTaskManager();

    private ScheduleTaskService taskService = Aop.get(ScheduleTaskService.class);

    private Map<String, Scheduler> schedulerMap = new ConcurrentHashMap<>();

    public ScheduleTaskManager(){
        initSchedules();
    }

    public static final ScheduleTaskManager me(){
        return manager;
    }


    private void initSchedules(){
        List<ScheduleTask> scheduleTaskList = taskService.findAll();
        if(scheduleTaskList== null || scheduleTaskList.isEmpty()){
            return;
        }
        for(ScheduleTask scheduleTask : scheduleTaskList){
            try {
                addTask(scheduleTask);
            }catch (Exception e){
                LOG.warn(e.getMessage(),e);
                continue;
            }

        }
    }

    public boolean trigger(ScheduleTask scheduleTask){
        if (!scheduleTask.isActive()) {
            return false;
        }

        try {
            Class taskClass =  Class.forName(scheduleTask.getTaskClass());
            Object task = ClassUtil.newInstance(taskClass);
            Scheduler scheduler = new Scheduler();
            scheduler.start();
            if(scheduleTask.isDistributed()){
                scheduler.launch(new Task() {
                    @Override
                    public void execute(TaskExecutionContext context) throws RuntimeException {
                        if(task instanceof Task){
                            new DistributedScheduleTask((Task) task).execute(context);
                        } else if(task instanceof Runnable){
                            new DistributedScheduleTask((Runnable) task).execute(context);
                        }else{
                            throw new IllegalStateException(" Task must be Runable,ProcessTask,ITask or Task");
                        }
                    }
                });
            }else{
                scheduler.launch(new Task(){

                    @Override
                    public void execute(TaskExecutionContext context) throws RuntimeException {
                        if(task instanceof Task){
                            ((Task) task).execute(context);
                        } else if(task instanceof Runnable){
                            ((Runnable) task).run();
                        }else{
                            throw new IllegalStateException(" Task must be Runable,ProcessTask,ITask or Task");
                        }

                    }
                });
            }

            return true;

        } catch (Exception e) {
            LOG.error("can not run task, class:" + scheduleTask.getTaskClass() + "\n" + e.toString(), e);
            throw new IllegalStateException(e);
        }

    }

    /**
     *
     * @param taskId 任务ID唯一
     * @param cron cron 表达式
     * @param task 执行的任务，必须是 Runnable,ProcessTask,ITask,或者 Task类型
     * @param listener 任务监听器，监听任务执行的状态
     * @param daemon 是否将任务执行线程设置为守护线程，守护线程会在 Tomcat关闭时自动关闭
     * @param enable 是否是开始执行
     * @param distributed 是否是分布式任务
     */
    public Scheduler addTask(String taskId, String cron, Object task, SchedulerListener listener, boolean daemon, boolean enable, boolean distributed){
        if(schedulerMap.get(taskId)!=null){
            throw new IllegalStateException(" Task:"+taskId+" is already in the current task schedule ");
        }
        Scheduler scheduler = new Scheduler();
        if(task instanceof Runnable){
            scheduler.schedule(cron, distributed? new JbootDistributedRunnable((Runnable) task) : (Runnable) task);
        }else if(task instanceof Task){
            scheduler.schedule(cron, distributed? new DistributedScheduleTask((Task)task) : (Task)task);
        }else{
            scheduler = null;
            throw new IllegalStateException(" Task must be Runable,ProcessTask,ITask or Task");
        }

        scheduler.addSchedulerListener(listener);
        if(enable){
            scheduler.start();
        }
        schedulerMap.put(taskId, scheduler);

        return scheduler;
    }

    public void addTask(ScheduleTask scheduleTask){
        if (!scheduleTask.isActive()) {
            return;
        }

        try {
            Class taskClass =  Class.forName(scheduleTask.getTaskClass());
            Object task = ClassUtil.newInstance(taskClass);
            addTask(scheduleTask.getId().toString(),
                    scheduleTask.getCron(),
                    task,
                    scheduleTask,
                    scheduleTask.isDaemon(),
                    scheduleTask.isStart(),
                    scheduleTask.isDistributed());
        } catch (ClassNotFoundException e) {
            LOG.error("can not create class:" + scheduleTask.getTaskClass() + "\n" + e.toString(), e);
            throw new IllegalStateException("can not create class:" + scheduleTask.getTaskClass());
        }
    }

    public void removeTask(ScheduleTask scheduleTask){
        removeTask(scheduleTask.getId().toString());
    }

    public void removeTask(String taskId){
        stop(taskId);
        schedulerMap.remove(taskId);
    }


    public void remove(){
        stop();
        schedulerMap.clear();
    }

    public boolean start(ScheduleTask scheduleTask){
        if(scheduleTask == null){
            return false;
        }
        if(schedulerMap.get(scheduleTask.getId())!=null) {
            return start(scheduleTask.getId().toString());
        }else{
            addTask(scheduleTask);
            return start(scheduleTask.getId().toString());
        }
    }

    public void start(){
        for(final String taskId : schedulerMap.keySet()){
            start(taskId);
        }
    }

    public boolean start(String taskId){
        final Scheduler scheduler = schedulerMap.get(taskId);
        if(scheduler !=null && !scheduler.isStarted()){
            scheduler.start();
            return scheduler.isStarted();
        }
        return false;
    }

    public boolean stop(String taskId){
        final Scheduler scheduler = schedulerMap.get(taskId);
        if(scheduler !=null && scheduler.isStarted()){
            scheduler.stop();
            return !scheduler.isStarted();
        }
        return false;
    }

    public boolean stop(ScheduleTask scheduleTask){
        if(scheduleTask !=null){
            return stop(scheduleTask.getId().toString());
        }
        return false;
    }

    public void stop(){
        for(final String taskId : schedulerMap.keySet()){
            stop(taskId);
        }
    }
    public Map<String,Scheduler> getSchedulerMap(){
        return schedulerMap;
    }
}
