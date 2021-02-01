/**
 *  Webex Parent
 *
 *  Copyright 2021 Ryan LaTorre
 *  Based on Twilio driver by Michael Ritchie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

def version() {"v1.0.20210201"}

metadata {
	definition (name: "Webex Parent", namespace: "rylatorr", author: "Ryan LaTorre") {
        attribute "containerSize", "number"	//stores the total number of child switches created by the parent
        command "createDevice", ["DEVICE LABEL", "WEBEX ROOM ID"] //create any new Virtual Device
    }
}

preferences {
  	input("authToken", "text", title: "Auth Token:", description: "Webex API Auth Token")
	input("isDebugEnabled", "bool", title: "Enable debug logging?", defaultValue: false, required: false)
}

def createDevice(deviceLabel, roomId){
    try{
    	state.vsIndex = state.vsIndex + 1	//increment even on invalid device type
		def deviceID = deviceLabel.toString().trim().toLowerCase().replace(" ", "_")
		logDebug "Attempting to create Virtual Device: Label: ${deviceLabel}, Room ID: ${roomId}"
		childDevice = addChildDevice("rylatorr", "Webex Device", "${deviceID}-${state.vsIndex}", [label: "${deviceLabel}", isComponent: true])
    	logDebug "createDevice Success"
		childDevice.updateSetting("roomId",[value:"${roomId}",type:"text"])
		logDebug "roomId Update Success"
    	updateSize()
    } catch (Exception e) {
         log.warn "Unable to create device."
    }
}

def installed() {
	logDebug "Installing and configuring Container"
    state.vsIndex = 0 //stores an index value so that each newly created Virtual Switch has a unique name (simply incremements as each new device is added and attached as a suffix to DNI)
    initialize()
}

def updated() {
	initialize()
}

def initialize() {
	logDebug "Initializing Container"
	updateSize()
}

def updateSize() {
	int mySize = getChildDevices().size()
    sendEvent(name:"containerSize", value: mySize)
}

def updatePhoneNumber() { // syncs device label with componentLabel data value
    def myChildren = getChildDevices()
    myChildren.each{
        if(it.label != it.data.label) {
            it.updateDataValue("roomId", it.label)
        }
    }
}


def sendNotification(roomId, message, deviceID) {
  	def postBody = [
        roomId: "${roomId}",
		text: "${message}"
  	]

  	def params = [
		uri: "https://webexapis.com/v1/messages",
    	headers: [
			"Authorization": "Bearer " + ("${authToken}").toString()
		],
        body: postBody
  	]

    if (authToken =~ /[A-Za-z0-9_\-]{64,106}/) {
        try {
            httpPostJson(params){response ->
                if (response.status != 200) {
                    log.error "Received HTTP error ${response.status}. Check your API Credentials!"
                } else {
                    def childDevice = getChildDevice(deviceID)
					if (childDevice) {
						childDevice.sendEvent(name:"message", value: "${message}", displayed: false)
					} else {
						log.error "Could not find child device: ${deviceID}"
					}
                    logDebug "Message Received by Webex: ${message}"
                }
            }
        } catch (Exception e) {
        	log.error "deviceNotification: Invalid API Credentials were probably entered. Webex Server Returned: ${e}"
			log.error "deviceNotification: params is: ${params}"
			log.error "deviceNotification: postbody is: ${postBody}"
		}
  	} else {
    	log.error "Auth Token '${authToken}' is not properly formatted!"
  	}
}

private logDebug(msg) {
	if (isDebugEnabled) {
		log.debug "$msg"
	}
}
