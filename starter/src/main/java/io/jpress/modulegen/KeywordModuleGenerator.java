/*
 * Copyright (c) 2018-2019, Eric 黄鑫 (ninemm@126.com).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.jpress.modulegen;

import io.jpress.codegen.ModuleGenerator;

/**
 * @author Eric Huang 黄鑫 （ninemm@126.com）
 * @version V1.0
 * @Package io.jboot.codegen
 */
public class KeywordModuleGenerator {


    private static String dbUrl = "jdbc:mysql://127.0.0.1:3306/keyword?useUnicode=true&useSSL=false&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";
    private static String dbUser = "root";
    private static String dbPassword = "123456";

    private static String moduleName = "keyword";
    private static String dbTables = "c_keyword,c_keyword_category";
    private static String modelPackage = "io.jpress.module.keyword.model";
    private static String servicePackage = "io.jpress.module.keyword.service";

    private static String tablePrefix = "c";


    public static void main(String[] args) {

        ModuleGenerator moduleGenerator = new ModuleGenerator(moduleName, dbUrl, dbUser, dbPassword, dbTables, modelPackage, servicePackage, tablePrefix, true);
        moduleGenerator.gen();

    }
}
