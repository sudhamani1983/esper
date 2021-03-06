/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.filter;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.core.engineimport.EngineImportService;
import com.espertech.esper.epl.expression.core.ExprEvaluatorContext;
import com.espertech.esper.epl.expression.core.ExprNode;
import com.espertech.esper.epl.variable.VariableService;

import java.lang.annotation.Annotation;

public class ExprNodeAdapterBaseVariables extends ExprNodeAdapterBase {
    protected final VariableService variableService;

    public ExprNodeAdapterBaseVariables(int filterSpecId, int filterSpecParamPathNum, ExprNode exprNode, ExprEvaluatorContext evaluatorContext, VariableService variableService, EngineImportService engineImportService, Annotation[] annotations) {
        super(filterSpecId, filterSpecParamPathNum, exprNode, evaluatorContext, engineImportService, annotations);
        this.variableService = variableService;
    }

    @Override
    public boolean evaluate(EventBean theEvent) {
        variableService.setLocalVersion();
        return evaluatePerStream(new EventBean[]{theEvent});
    }
}
