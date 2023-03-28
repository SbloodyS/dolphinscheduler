/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.common.os;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.util.Arrays;


/**
 * os information test
 */
public class OshiTest {

    private static Logger logger = LoggerFactory.getLogger(OshiTest.class);


    @Test
    public void test() {

        SystemInfo si = new SystemInfo();

        HardwareAbstractionLayer hal = si.getHardware();

        logger.info("Checking Memory...");
        printMemory(hal.getMemory());


        logger.info("Checking CPU...");
        printCpu(hal.getProcessor());

    }



    private static void printMemory(GlobalMemory memory) {

        logger.info("memory avail:{} MB" , memory.getAvailable() / 1024 / 1024 );//memory avail:6863 MB
        logger.info("memory total:{} MB" , memory.getTotal() / 1024 / 1024 );//memory total:16384 MB
    }
    
}
