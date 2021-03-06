import groovy.json.JsonBuilder

/**
 * Generate Patient resources from CSV table
 *
 * Input format
 * Column 1 Blue ID
 * Column 2 Green ID
 * Column 3 Red ID
 * Column 4 Last Name
 * Column 5 First Name
 */

class MyPatient {
    String pid
    String first
    String last

    MyPatient(def _pid, String _name) {
        String[] parts = _name.split(' ')
        def _first = parts[0]
        def _last = parts[1]
        pid = _pid
        first = _first
        last = _last

        println "${pid} - ${first} ${last}"
    }

    String getSystem() {
        String aa = getAA()
        def (x, oid, iso) = aa.tokenize('&')
        return "urn:oid:${oid}"
    }

    String getValue() {
        String[] parts = pid.split('\\^')
        if (parts.length == 0) return ''
        return parts[0]
    }

    String getAA() {
        String[] parts = pid.split('\\^')
        if (parts.length < 4) return ''
        return parts[3]
    }
}

// MyPatient objects
def red = []
def green = []
def blue = []

// Load from CSV file  (filename is argument #1)

File csvInFile = new File(args[0])
File outDir = new File(args[1])

if (!outDir.exists() || !outDir.isDirectory()) {
    println "Output Directory (${outDir}) does not exist or is not a directory."
}

int line=0
csvInFile.splitEachLine(",") { fields ->
    if (line != 0) {
        blue.add( new MyPatient(stripPI(fields[0]), fields[3]))
        green.add(new MyPatient(stripPI(fields[1]), fields[3]))
        red.add(  new MyPatient(stripPI(fields[2]), fields[3]))
    }
    line++
}

def documentation = buildPatientTableHeader()

int myId = 1
red.each { MyPatient patient ->
    JsonBuilder json
    def data
    (data, json) = patientToJson(patient, myId)
    documentation += buildPatientTable(data)
    println json.toPrettyString()
    File outFile = new File(outDir, 'Patient' + Integer.toString(myId) + '.json')
    outFile.text = json.toPrettyString()
    myId++
}

green.each { MyPatient patient ->
    JsonBuilder json
    def data
    (data, json) = patientToJson(patient, myId)
    documentation += buildPatientTable(data)
    println json.toPrettyString()
    File outFile = new File(outDir, 'Patient' + Integer.toString(myId) + '.json')
    outFile.text = json.toPrettyString()
    myId++
}

blue.each { MyPatient patient ->
    JsonBuilder json
    def data
    (data, json) = patientToJson(patient, myId)
    documentation += buildPatientTable(data)
    println json.toPrettyString()
    File outFile = new File(outDir, 'Patient' + Integer.toString(myId) + '.json')
    outFile.text = json.toPrettyString()
    myId++
}

println 'DOCUMENTATION'
println documentation
new File('documentation.md').text = documentation


static buildPatientTableHeader() {
    return 'Id | Given Name | Family Name | PatientID System | PatientID Value ' + '\r\n' +
            ' -- | -- | -- | -- | -- ' + '\r\n'
}

// data is the raw content generated by patientToJson
static buildPatientTable(def data) {
    return "${resourceLink(data.id)} | ${data.name[0].given[0]} | ${data.name[0].family[0]} | ${data.identifier[0].system} | ${data.identifier[0].value}" + '\r\n'
}

static resourceLink(def id) {
    "[${id}](http://172.16.0.123:9080/ftoolkit/fhir/fsim/nouser__cat/fhir/put/Patient/${id})"
}
//[foo](http://foo)


def patientToJson(MyPatient patient, int myId) {
    def data = [
            resourceType: 'Patient',
            id: myId,
            identifier:  ['a'].collect { String ignore ->
                [
                        use: 'usual',
                        system: patient.system,
                        value: patient.value
                ]
            },
            active: true,
            name: ['a'].collect { String ignore ->
                [
                        use: 'official',
                        family: [patient.last].collect { String name -> name},
                        given: [patient.first].collect { String name -> name}
                ]
            }
    ]
    return [ data, new JsonBuilder(data) ]
}


static String stripPI(String pid) {
    if (pid.endsWith('^PI')) return pid[0..-4]
    return pid
}




