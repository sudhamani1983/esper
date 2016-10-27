/*
 * *************************************************************************************
 *  Copyright (C) 2006-2015 EsperTech, Inc. All rights reserved.                       *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.regression.datetime;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.scopetest.EPAssertionUtil;
import com.espertech.esper.client.scopetest.SupportUpdateListener;
import com.espertech.esper.client.time.CurrentTimeEvent;
import com.espertech.esper.client.util.DateTime;
import com.espertech.esper.metrics.instrumentation.InstrumentationHelper;
import com.espertech.esper.support.bean.SupportDateTime;
import com.espertech.esper.support.bean.lambda.LambdaAssertionUtil;
import com.espertech.esper.support.client.SupportConfigFactory;
import junit.framework.TestCase;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;

public class TestDTPlusMinus extends TestCase {

    private EPServiceProvider epService;
    private SupportUpdateListener listener;

    public void setUp() {

        Configuration config = SupportConfigFactory.getConfiguration();
        config.addEventType("SupportDateTime", SupportDateTime.class);
        epService = EPServiceProviderManager.getDefaultProvider(config);
        epService.initialize();
        listener = new SupportUpdateListener();
        if (InstrumentationHelper.ENABLED) { InstrumentationHelper.startTest(epService, this.getClass(), getName());}
    }

    public void tearDown() {
        if (InstrumentationHelper.ENABLED) { InstrumentationHelper.endTest();}
        listener = null;
    }

    public void testPlusMinus() {

        epService.getEPAdministrator().createEPL("create variable long varmsec");
        String startTime = "2002-05-30T09:00:00.000";
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(DateTime.parseDefaultMSec(startTime)));

        String[] fields = "val0,val1,val2,val3,val4,val5,val6,val7,val8,val9,val10,val11".split(",");
        String eplFragment = "select " +
                "current_timestamp.plus(varmsec) as val0," +
                "utildate.plus(varmsec) as val1," +
                "msecdate.plus(varmsec) as val2," +
                "caldate.plus(varmsec) as val3," +
                "localdate.plus(varmsec) as val4," +
                "zoneddate.plus(varmsec) as val5," +
                "current_timestamp.minus(varmsec) as val6," +
                "utildate.minus(varmsec) as val7," +
                "msecdate.minus(varmsec) as val8," +
                "caldate.minus(varmsec) as val9," +
                "localdate.minus(varmsec) as val10," +
                "zoneddate.minus(varmsec) as val11" +
                " from SupportDateTime";
        EPStatement stmtFragment = epService.getEPAdministrator().createEPL(eplFragment);
        stmtFragment.addListener(listener);
        LambdaAssertionUtil.assertTypes(stmtFragment.getEventType(), fields, new Class[]{Long.class, Date.class, Long.class, Calendar.class, LocalDateTime.class, ZonedDateTime.class,
                Long.class, Date.class, Long.class, Calendar.class, LocalDateTime.class, ZonedDateTime.class});

        epService.getEPRuntime().sendEvent(SupportDateTime.make(null));
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), fields, new Object[]{SupportDateTime.getValueCoerced(startTime, "msec"), null, null, null, null, null,
                SupportDateTime.getValueCoerced(startTime, "msec"), null, null, null, null, null,});

        Object[] expectedPlus = SupportDateTime.getArrayCoerced(startTime, "msec", "util", "msec", "cal", "ldt", "zdt");
        Object[] expectedMinus = SupportDateTime.getArrayCoerced(startTime, "msec", "util", "msec", "cal", "ldt", "zdt");
        epService.getEPRuntime().sendEvent(SupportDateTime.make(startTime));
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), fields, EPAssertionUtil.concatenateArray(expectedPlus, expectedMinus));

        epService.getEPRuntime().setVariableValue("varmsec", 1000);
        epService.getEPRuntime().sendEvent(SupportDateTime.make(startTime));
        //System.out.println("===> " + SupportDateTime.print(listener.assertOneGetNew().get("val4")));
        expectedPlus = SupportDateTime.getArrayCoerced("2002-05-30T09:00:01.000", "msec", "util", "msec", "cal", "ldt", "zdt");
        expectedMinus = SupportDateTime.getArrayCoerced("2002-05-30T08:59:59.000", "msec", "util", "msec", "cal", "ldt", "zdt");
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), fields, EPAssertionUtil.concatenateArray(expectedPlus, expectedMinus));

        epService.getEPRuntime().setVariableValue("varmsec", 2*24*60*60*1000);
        epService.getEPRuntime().sendEvent(SupportDateTime.make(startTime));
        expectedMinus = SupportDateTime.getArrayCoerced("2002-05-28T09:00:00.000", "msec", "util", "msec", "cal", "ldt", "zdt");
        expectedPlus = SupportDateTime.getArrayCoerced("2002-06-1T09:00:00.000", "msec", "util", "msec", "cal", "ldt", "zdt");
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), fields, EPAssertionUtil.concatenateArray(expectedPlus, expectedMinus));
    }

    public void testPlusMinusTimePeriod() {

        String startTime = "2002-05-30T09:00:00.000";
        epService.getEPRuntime().sendEvent(new CurrentTimeEvent(DateTime.parseDefaultMSec(startTime)));

        String[] fields = "val0,val1,val2,val3,val4,val5,val6,val7,val8,val9,val10,val11".split(",");
        String eplFragment = "select " +
                "current_timestamp.plus(1 hour 10 sec 20 msec) as val0," +
                "utildate.plus(1 hour 10 sec 20 msec) as val1," +
                "msecdate.plus(1 hour 10 sec 20 msec) as val2," +
                "caldate.plus(1 hour 10 sec 20 msec) as val3," +
                "localdate.plus(1 hour 10 sec 20 msec) as val4," +
                "zoneddate.plus(1 hour 10 sec 20 msec) as val5," +
                "current_timestamp.minus(1 hour 10 sec 20 msec) as val6," +
                "utildate.minus(1 hour 10 sec 20 msec) as val7," +
                "msecdate.minus(1 hour 10 sec 20 msec) as val8," +
                "caldate.minus(1 hour 10 sec 20 msec) as val9," +
                "localdate.minus(1 hour 10 sec 20 msec) as val10," +
                "zoneddate.minus(1 hour 10 sec 20 msec) as val11" +
                " from SupportDateTime";
        EPStatement stmtFragment = epService.getEPAdministrator().createEPL(eplFragment);
        stmtFragment.addListener(listener);
        LambdaAssertionUtil.assertTypes(stmtFragment.getEventType(), fields, new Class[]{Long.class, Date.class, Long.class, Calendar.class, LocalDateTime.class, ZonedDateTime.class,
                Long.class, Date.class, Long.class, Calendar.class, LocalDateTime.class, ZonedDateTime.class});

        epService.getEPRuntime().sendEvent(SupportDateTime.make(startTime));
        Object[] expectedPlus = SupportDateTime.getArrayCoerced("2002-05-30T010:00:10.020", "msec", "util", "msec", "cal", "ldt", "zdt");
        Object[] expectedMinus = SupportDateTime.getArrayCoerced("2002-05-30T07:59:49.980", "msec", "util", "msec", "cal", "ldt", "zdt");
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), fields, EPAssertionUtil.concatenateArray(expectedPlus, expectedMinus));

        epService.getEPRuntime().sendEvent(SupportDateTime.make(null));
        expectedPlus = SupportDateTime.getArrayCoerced("2002-05-30T010:00:10.020", "msec", "null", "null", "null", "null", "null");
        expectedMinus = SupportDateTime.getArrayCoerced("2002-05-30T07:59:49.980", "msec", "null", "null", "null", "null", "null");
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), fields, EPAssertionUtil.concatenateArray(expectedPlus, expectedMinus));
    }
}
