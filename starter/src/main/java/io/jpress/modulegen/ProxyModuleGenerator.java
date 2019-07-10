package io.jpress.modulegen;

import io.jpress.codegen.ModuleGenerator;

/**
 * @author Eric Huang 黄鑫 （ninemm@126.com）
 * @version V1.0
 * @Package io.jboot.codegen
 */
public class ProxyModuleGenerator {
	  
	private static String dbUrl = "jdbc:mysql://127.0.0.1:3306/proxy?useInformationSchema=true";
	private static String dbUser = "root";
	private static String dbPassword = "123456";

	private static String moduleName = "proxy";
	private static String dbTables = "proxy_info";
	private static String modelPackage = "io.jpress.module.proxy.model";
	private static String servicePackage = "io.jpress.module.proxy.service";

	public static void main(String[] args) {
		ModuleGenerator moduleGenerator = new ModuleGenerator(moduleName, dbUrl, dbUser, dbPassword, dbTables, modelPackage, servicePackage,true);
		moduleGenerator.gen();
	}

}
