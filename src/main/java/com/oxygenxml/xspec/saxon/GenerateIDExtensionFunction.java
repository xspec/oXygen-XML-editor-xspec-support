package com.oxygenxml.xspec.saxon;

import com.oxygenxml.xspec.XSpecUtil;

import net.sf.saxon.expr.StaticProperty;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.StringValue;

public class GenerateIDExtensionFunction extends net.sf.saxon.lib.ExtensionFunctionDefinition {
  private static final String NS = "http://www.oxygenxml.com./xslt/xspec";
  private static final String NAME = "generate-id";

  @Override
  public StructuredQName getFunctionQName() {
    return new StructuredQName("", NS, NAME);
  }

  @Override
  public net.sf.saxon.value.SequenceType getResultType(
      net.sf.saxon.value.SequenceType[] arg0) {
    return net.sf.saxon.value.SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE);
    }

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ExtensionFunctionCall() {
      @Override
      public Sequence call(XPathContext context, Sequence[] arguments)
          throws XPathException {

        String arg = arguments[0].head().getStringValue();
        
        return new StringValue(XSpecUtil.generateId(arg).toString());
      }
    };
  }

  @Override
  public net.sf.saxon.value.SequenceType[] getArgumentTypes() {
    return new net.sf.saxon.value.SequenceType[] {
        net.sf.saxon.value.SequenceType.makeSequenceType(BuiltInAtomicType.STRING, StaticProperty.EXACTLY_ONE)};
  }
}
