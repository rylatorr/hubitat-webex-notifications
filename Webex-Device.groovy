/**
*  Webex Device
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
*  for the specific language governing permissions an limitations under the License.
*
*
*/
def version() {"v1.0.20210129"}

metadata {
  	definition (name: "Webex Device", namespace: "rylatorr", author: "Ryan LaTorre") {
    	capability "Notification"
  	}
	
	attribute "lastMessage", "string"
}

preferences {
	input("roomId", "text", title: "Room ID:", description: "Webex Room ID (from API) to send messages to.", required: true)
}

def installed() {
    initialize()
}

def updated() {
 	initialize()
}

def initialize() {
    state.version = version()
}

def deviceNotification(message) {
  	parent.sendNotification(roomId, message, device.deviceNetworkId)
}

