package gov.nist.toolkit.errorrecording

import gov.nist.toolkit.errorrecording.client.XMLValidatorErrorItem
import gov.nist.toolkit.errorrecording.client.XdsErrorCode.Code
import gov.nist.toolkit.errorrecording.client.structures.ReportingCompletionType
import gov.nist.toolkit.errorrecording.client.structures.ReportingLevel
import gov.nist.toolkit.errorrecording.factories.ErrorRecorderBuilder
import gov.nist.toolkit.xdsexception.ExceptionUtil
import groovy.xml.MarkupBuilder
import org.apache.log4j.Logger

/**
 * Created by diane on 2/19/2016.
 */
public class XMLErrorRecorder implements ErrorRecorder {
    public ErrorRecorderBuilder errorRecorderBuilder;

    static Logger logger = Logger.getLogger(XMLErrorRecorder.class);


    //def summary = // former List<GwtValidatorErrorItem> summary = new ArrayList<>();
    //List<ErrorRecorder> children = new ArrayList<>();  // Probably not useful in new XML validator and should be removed
    def errXml = '''<ErrorLog></ErrorLog>'''
    def errMsgs = new XmlParser().parseText(errXml) // should be called ErrorLog to be accurate
    def errRecords = errMsgs.children()


    /**
     * //TODO
     * Temporary toString function for testing purposes, may need upgrade later
     * @return
     */
    @Override
    def String toString() {
        // Convert back to String / XML
        StringWriter sw = new StringWriter()
        new XmlNodePrinter(new PrintWriter(sw)).print(errMsgs)

        //println("\n--- XML output: ---\n\n" + sw.toString())
        return sw.toString()
    }


    @Override
    public void err(Code code, String msg, String location, String resource, Object log_message) {
        println("err")
        // Check if error message is not null
        if (msg == null || msg.trim().equals(""))
            return;

        // Set parameters on the XMLValidatorErrorItem and run it
        XMLValidatorErrorItem ei = new XMLValidatorErrorItem();
        ei.level = XMLValidatorErrorItem.ReportingLevel.ERROR;
        ei.msg = msg;
        ei.setCode(code);
        ei.location = location;
        ei.resource = resource;
        ei.completion = XMLValidatorErrorItem.ReportingCompletionType.ERROR;

        // add result to the list of errors
        addElement(ei);

        // errorcount++
        lastErrCount++;

        // propagate error to Challenge level
        propagateError();
    }

    def addElement(XMLValidatorErrorItem ei){

    }

    @Override
    public void err(Code _code, String _msg, String _location, String _resource) {
        println("err2")
       if (_msg == null || _msg.trim().equals("")) { return; }

        // Generate the new element
        def sw = new StringWriter()
        def builder = new MarkupBuilder(sw)
        builder.Error(code:_code, location:_location, resource:_resource){
            Message(_msg)
        }

        // Parse and add
        def el = new XmlParser().parseText(sw.toString())
        errRecords.add(el)
    }

    @Override
    public void err(Code code, String msg, Object location, String resource) {
        println("err3")
        String loc = getSimpleName(location)
        err(code, msg, loc, resource);
    }

    @Override
    public void err(Code code, Exception e) {
        println("err4")
        err(code, ExceptionUtil.exception_details(e), null, "");
    }

    @Override
    public void err(Code _code, String _msg, String _location, String _severity, String _resource) {
        println("err5")
        err(_code.toString(), _msg, _location, _severity, _resource)
    }

    // Untested, need real test data to complete
    @Override
    public void err(String _code, String _msg, String _location, String _severity, String _resource) {
        println("err6")
        if (_msg == null || _msg.trim().equals(""))
            return;

        // Log errors if any
        logger.debug(ExceptionUtil.here("err - " + _msg));
        if (_severity.indexOf("Error") != -1)
            System.out.println("Got Error");

        // Prepare parameters for logging
        boolean isWarning = (_severity == null) ? false : ((_severity.indexOf("Warning") != -1));

        // Generate the new element
        if (isWarning) {
            println("this should print a warning")
           // warning(_code, _msg, _location, _resource)
        }
        else {
            println("this should print an error")
            //err(_code, _msg, _location, _resource)
        }    }

    @Override
    public void warning(String _code, String _msg, String _location, String _resource) {
        println("warning1")
        err(_code, _msg, _location, "Warning", _resource);
    }

    @Override
    public void warning(Code _code, String _msg, String _location, String _resource) {
        println("warning2")
        err(_code.toString(), _msg, _location, "Warning", _resource);
    }

    private void propagateError() {
        println("NYI-propagateerr")
        // Test if in a section heading or challenge section. If challenge then set the ReportingCompletionType to Error.
    }



    //TODO last because a SectionHeading element needs to wrap an entire section
    @Override
    public void sectionHeading(String msg) {
        println("sectionheading")
        //tagLastInfo2(); // TODO let's see if it works without saving location of item
        def newElement = '''<SectionHeading>''' + msg + '''</SectionHeading>'''
        def newRecord = new XmlParser().parseText(newElement)
        errRecords.add(newRecord)
    }

    @Override
    public void challenge(String msg) {
        println("challenge")
        errRecords.add(createXMLElement("Challenge", msg))
    }

    @Override
    public void externalChallenge(String msg) {
        println("extchallenge")
        errRecords.add(createXMLElement("ExternalChallenge", msg))
    }

    @Override
    public void detail(String msg) {
        println("detail")
        errRecords.add(createXMLElement("Detail", msg))
    }

    @Override
    public void report(String name, String found) {
        println("report")
        detail(name + " " + found);
    }

    /**
     * 	Note: This section was stored but not displayed when using the GWTErrorRecorder
     * @param dts
     * @param name
     * @param found
     * @param expected
     * @param RFC
     */
    @Override
    public void success(String _dts, String _name, String _found, String _expected, String _rfc) {
        println("success")
        def sw = new StringWriter()
        def builder = new MarkupBuilder(sw)
        builder.records() {
            Success (name:_name, dts:_dts, found:_found, expected:_expected, rfc:_rfc)
        }
        def el = new XmlSlurper().parseText(sw.toString())
        errRecords.add(el)
    }

    @Override
    public void error(String _dts, String _name, String _found, String _expected, String _rfc) {
        println("error")
        def sw = new StringWriter()
        def builder = new MarkupBuilder(sw)
        builder.records() {
            Error (name:_name, dts:_dts, found:_found, expected:_expected, rfc:_rfc)
        }
        def el = sw.write()
        errRecords.add(el)
    }

    @Override
    public void test(boolean good, String dts, String name, String found, String expected, String RFC) {
        println("NYI-test")
    }

    @Override
    public void warning(String _dts, String _name, String _found, String _expected, String _rfc) {
        println("warning")
        def sw = new StringWriter()
        def builder = new MarkupBuilder(sw)
        builder.records() {
            Warning (name:_name, dts:_dts, found:_found, expected:_expected, rfc:_rfc)
        }
        def el = sw.write()
        errRecords.add(el)
    }

    @Override
    public void info(String _dts, String _name, String _found, String _expected, String _rfc) {
        println("info")
        def sw = new StringWriter()
        def builder = new MarkupBuilder(sw)
        builder.records() {
            Info (name:_name, dts:_dts, found:_found, expected:_expected, rfc:_rfc)
        }
        def el = sw.write()
        errRecords.add(el)
    }

    @Override
    public void summary(String _msg, boolean _success, boolean _part) {
        println("summary")
        def sw = new StringWriter()
        def builder = new MarkupBuilder(sw)
        builder.records() {
            Summary (success:_success, part:_part){
                Message(_msg)
            }
        }
        def el = sw.write()
        errRecords.add(el)
    }

    @Override
    public void finish() {
        println("NYI-finish")
    }

    @Override
    public void showErrorInfo() {
        println("NYI-showerrinfo")
    }

    @Override
    public boolean hasErrors() {
        println("NYI-boolhaserrors")
        return false;
    }

    @Override
    public int getNbErrors() {
        println("NYI-getnberrors")
        return 0;
    }

    public void concat(ErrorRecorder er) {
        println("NYI-concat")
    }

    public List<XMLValidatorErrorItem> getErrMsgs() {
        println("NYI-geterrmsgs")
        return null;
    }

    @Override
    public List<ErrorRecorder> getChildren() {
        println("NYI-errrecorder")
        return null;
    }

    @Override
    public int depth() {
        println("NYI-depth")
        return 0;
    }

    @Override
    public void registerValidator(Object validator) {
        println("NYI-regvalidator")
    }

    @Override
    public void unRegisterValidator(Object validator) {
        println("NYI-unregvalidator")
    }

    @Override
    public ErrorRecorder buildNewErrorRecorder() {
        XMLErrorRecorder rec =  new XMLErrorRecorder();
        rec.errorRecorderBuilder = this;
        return rec;
    }

    @Override
    public ErrorRecorder buildNewErrorRecorder(Object o) {
        ErrorRecorder er =  errorRecorderBuilder.buildNewErrorRecorder();
        children.add(er);
        return er;
    }

    // ---------- Utility functions -----------

    /**
     * Utility function
     * @param name The name of the new XML element to create.
     * @return The new XML element.
     */
    def private static Node createXMLElement(String name, String msg){
        def newElement = "<" + name + ">" + msg + "</" + name + ">"
        def newRecord = new XmlParser().parseText(newElement)
        return newRecord
    }

    /**
     * Groovy MarkupBuilder writes its output inside a StringWriter.
     */
    private String nodeToString(StringWriter sw, String node){
        new XmlNodePrinter(new PrintWriter(sw)).print(node)

    }


    private String getSimpleName(Object location){
        if (location != null)
            return location.getClass().getSimpleName();
    }

    /**
     * Parses an xml string and replaces &lt; and &gt; characters with readable < and >
     * @param xml
     * @return Readable XML
     */
    private String formatXmlTags(String xml){
        return xml.replace( '&lt;', '<' ).replace( '&gt;', '>' )
    }

}