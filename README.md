# Robotics-Events
[![Build Status](https://travis-ci.com/eclipse-researchlabs/brain-iot-robotics-events.svg?branch=main)](https://travis-ci.com/eclipse-researchlabs/brain-iot-robotics-events)

## Prerequisites

The repository will be built using bnd version 5.1.2, so the Maven version must be at least 3.5.4

###Setup

Install Maven 3.6.3:

```bash
$ wget https://downloads.apache.org/maven/maven-3/3.6.3/binaries/apache-maven-3.6.3-bin.tar.gz -P /tmp
$ tar xf /tmp/apache-maven-3.6.3-bin.tar.gz -C /usr/share
```
Then configure environment variables, open **/etc/profile**, and add new lines, then save:
```bash
$ export PATH=/usr/share/apache-maven-3.6.3/bin:$PATH
$ export MAVEN_HOME=/usr/share/apache-maven-3.6.3
```
to activate the changes, run **source /etc/profile** or log out your account and log in again

Check Maven version:
```bash
$ mvn -version
``` 


This repository defines the Events in the `eu.brain.iot.robot.api` sub-project to be used in the updated Service Robotics use case. It contains the following packages:

* **eu.brain.iot.robot.events**  (contains events between robot behaviour and ROS Edge Node)
* **eu.brain.iot.warehouse.events**   (contains events between robot behaviour and Warehouse Manager)

This repository also contains the door.api, door.impl, Robot Behaviour, single-framework-example projects updated based on the M18 version, to be used in updated use case later.

### Workflow update

*According to the final demo, the negotiation procedure between ROS Edge Node and Robot Behavior has been reverted, this has been updated on the top of **Page 1** of the following HTML file.*


The latest Robot Behaviour Workflow, which is exported as a HTML file with multiple pages"

* **Page 1**: Robot Behaviour Workflow in simulation, without checking door status and PickCart event hsa cart marker ID property
* **Page 2**: Robot Behaviour Workflow in real robot, checking door status and all PickCart event are same, except the robotID

Note: to simplify the development of the robot behaviour, the `PickCart` event could be always contains the cart ID property in both Simulation and Real robot, ROS Edge Node will ignore this property when it's not needed.

* **Page 3**: Service Robotic Architecture with SensiNact
* **Page 4**: Sequence Diagram with simplified Pickup Mission Execution
* **Page 5**: Sequence Diagram with complete Pickup Mission Execution steps, involving SensiNact, Warehouse Backend, Robot Beahviour, Ros Edge Node, Door Edge Node
* **Page 6**: Copy of **Page 5**, but also appended with the `Go back to Docking Area` sequence Diagram
* **Page 7**: Copy of **Page 5**, but also involing the Physical Robot, and the detailed `Pickup Mission Execution` steps between ROS Edge Node and Physical Robot are also presented in  Physical Robot


To see the different pages, download the `Robotic_Workflows_SIM_and_RealRobot_Sequence_Diagrams.html` file and open it in browser, in the left-top of the fist page, there is the options to switch between different pages.

![image](./options.png)

To edit the diagram, download the source file `Robotic_Workflows_SIM_and_RealRobot_Sequence_Diagrams_latest.drawio`, and open it in the Google APP `Diagrams.net Desktop `.


## Integration Test in Simulation
clone the repository, it contains the following main bundles:

* 1-door.api
* 2-door.impl
* 3-eu.brain.iot.robot.api
* 4-eu.brain.iot.robot.behaviour
* 5-eu.brain.iot.robot.tables.creator
* 6-eu.brain.iot.robot.tables.queryer
* 7-eu.brain.iot.single-framework-example
* 8-eu.brain.iot.warehouse.sensinact.api

As for other bundles missing in the list, they're used for test now.
 
Sepcially, the warehouse backend is composed of above two bundles: 

**Tables.Creator**: it just creates the 4 tables (H2 DB) after it’s deployed. 

**Tables.Queryer**: it will connect to the tables and integrate with Robot Behavior to provide coordinate info, and it also updates the Picking_Points table after one iteration starts or is finished 

## Configuration

#### (1) Configure **Tables.Creator** bundle to create tables (see wiki)

Copy the **resources** folder in the **eu.brain.iot.robot.tables.creator** into your *HOME* directory, there're four JSON files storing the corresponding table values to be used in the simulation. This bundle will always use the fixed path **~/resources** to create the tables.

#### (2) Configure ROS Edge Node

Currently the ROS Edge Node has been updated to be able to read the robot IP, robot ID and robot name from an external txt file **~/rosConfig.txt**.

Create a **rosConfig.txt** file in *HOME* directory, and add the following content:
```bash
robotIP=localhost
robotId=1
robotName=rb1_base_a
```
Note: when ROS Edge Node is launched, it automatically read this file to configure itself and broadcast the ready info to Robot Behavior. There will be only one robot started in silution when launching the application, because only one ROS Edge Node instance will be created in a single framework. Multiple robots will be tested in Fabric.

#### (3) Configure Door and Tables Creator

Enter **eu.brain.iot.single-framework-example** project and open **src/main/resources/OSGI-INF/configurator/configuration.json**, and change the **jsonFilePath** to your own path

```json
{
    ":configurator:resource-version" : 1,
    ":configurator:symbolic-name" : "eu.brain.iot.service.robotic.eu.brain.iot.single-framework-example.config",
    ":configurator:version" : "0.0.2-SNAPSHOT",
    
    "eu.brain.iot.example.robot.Door": {
    	"host": "localhost", 
        "port": "8080",
        "id": "ExampleDoor",
        "path": "/door1/door_controller/command"
     },

    "eu.brain.iot.robot.tables.creator.TablesCreater": {
           "jsonFilePath": "/home/rui/resources/"
    }    
}
```

## Build and Run
The ROS Edge Node has been added as a dependency in **eu.brain.iot.single-framework-example** project, and it could be launched in the bndrun files in this project together with the other components: tables.creator, tables.queryer, robot behavior and door.

Moreover, the tables.creator bundle can execute separatly without running it every time when you start components, since only one robot will be started each time and there're three available carts.

#### (1) Run Tables.Creator

Open pom.xml of **eu.brain.iot.single-framework-example** project and change the configuration of bndrun file for both **bnd-resolver-maven-plugin** and **bnd-export-maven-plugin** to **TablesCreator.bndrun**, then save and build in project root folder:
```bash
$ cd brain-iot-robotics-events
$ mvn initialize
$ mvn clean install
$ cd eu.brain.iot.single-framework-example/target
$ ls
TablesCreator.jar  eu.brain.iot.single-framework-example-0.0.2-SNAPSHOT.jar  ......
$ java -jar TablesCreator.jar
--------------------Warehouse Tables--------------------------
......
-----------------------------------------------
jsonFilePath = /home/rui/resources/
Table Creator is creating /home/rui/tables.mv.db..........
------------  PickingTable ----------------
PP1, 8.0,-3.6,-3.14, FALSE
PP2, 8.0,-5.5,-3.14, FALSE
PP3, 8.0,-7.75,-3.14, FALSE
------------  StorageTable ----------------
ST1, 7.0,-0.6,-3.14, 0.0,0.0,-3.14
ST2, 7.0,0.0,-3.14, 0.0,-7.75,-3.14
ST3, 7.0,0.6,-3.14, 0.0,7.75,-3.14
------------  CartTable ----------------
2, ST2
3, ST1
4, ST3
------------  DockTable ----------------
1, 5.0,-0.6,-3.14, 8.0,0.0,-3.14
2, 5.0,0.0,-3.14, 8.0,1.0,-3.14
3, 5.0,0.6,-3.14, 8.0,2.0,-3.14
Table Creator finished to create /home/rui/tables..........

Hello, this is Table Queryer !
Table Queryer is reading /home/rui/tables.mv.db..........
+++++++++ Table Queryer filter = (robotID=*)
------------  PickingTable ----------------
PP1, 8.0,-3.6,-3.14, FALSE
PP2, 8.0,-5.5,-3.14, FALSE
PP3, 8.0,-7.75,-3.14, FALSE
```
As can be seen from the logs, the **TablesCreator.bndrun** will launch the table creator and queryer together, then four tables will be created in the **~/tables.mv.db** database, with all carts haven't been picked (FALSE in the PickingTable).

Then input *CTRL+C* to stop them. 

Copy the generated executable **target/TablesCreator.jar** file into your *HOME* directory, you could run this jar whenever all the rows have the *TRUE* value in **PickingTable**. Because Tables.Queryer will return the picking point coordinate when the row has the *FALSE* value, otherwise there is not any available cart to be picked.

Keep this jar file, and it could be used for many times.

#### (2) Run ROS Edge Node, Tables.Queryer, Robot Behavior and Door

Open pom.xml of **eu.brain.iot.single-framework-example** project and change the configuration of bndrun file for both **bnd-resolver-maven-plugin** and **bnd-export-maven-plugin** to **robotbehavior-rosEdgeNode-door-queryer.bndrun**, then save and build from project root folder or this sub-project:
```bash
$ cd brain-iot-robotics-events
$ mvn clean package
$ cd eu.brain.iot.single-framework-example/target
$ ls
robotbehavior-rosEdgeNode-door-queryer.jar  ......
$ java -jar robotbehavior-rosEdgeNode-door-queryer.jar
roscore config file: /home/rui/rosConfig.txt
robotIP=192.168.2.202
robotId=1
robotName=rb1_base_a
...
--------------------Internal CartMapper---------------------------
{2=rb1_base_a_cart2_contact, 3=rb1_base_b_cart3_contact, 4=rb1_base_c_cart4_contact}
-----------------------------------------------
Hello!  I am ROS Edge Node : 1  name = rb1_base_a  IP = 192.168.2.202,  UUID = b27ee227-8a3a-4eef-ad0d-71c21b599cae
+++++++++ Ros Edge Node filter = (|(robotID=1)(robotID=-1))
...
Hello!  I am robotBehavior : 0,  UUID = b27ee227-8a3a-4eef-ad0d-71c21b599cae
+++++++++ Robot Behaviour filter =             # robotBehavior hasn't been configed by ROS Edge Node

Hello, this is Table Queryer !
Table Queryer is reading /home/rui/tables.mv.db..........
...
 The ROS Edge Node is registering....for Robot 1
GoToComponent service registed.
PickComponent service registed.

{
  "data" : 0.0
}
+++++++++ Table Queryer filter = (robotID=*)
...
DOOR REST Service Invoked Successfully..

PlaceComponent service registed.
PP1, 8.0,-3.6,-3.14, FALSE
PP2, 8.0,-5.5,-3.14, FALSE
PP3, 8.0,-7.75,-3.14, FALSE

 >>> robot_1 broadCast Ready info
-->RB 0 received an event: RobotReadyBroadcast    # robotBehavior received broadcast info, and configure itself with robotID=1
-->RB 1 update properties = {eu.brain.iot.behaviour.filter=(|(robotID=1)(robotID=-1))}
-->RB 1 robotReady -- true

--------------------------- Query Pick point --------------------------------------
-->RB1 is waiting PickResponse
--> Table Queryer received an event class eu.brain.iot.warehouse.events.NewPickPointRequest
--> Table Queryer got a pickPoint 8.0,-3.6,-3.14
...
```
See **log.out** file in this repo the full logs of a compelete iteration of moving a cart and going back to docking area.

There're also the **RobotBehaviour.bndrun** and **rosEdgeNode.bndrun** to be used for launching the components separatly for tests.

## TroubleShooting

Note: the `smart-behaviour-maven-plugin` has already been added in the pom.xml of robot behaviour project to package it as a smart behaviour, each build must have the `clean` phase to remove the `smart-behaviour` directory in target folder

If there is the errors generated by the **bnd-export-maven-plugin** when exporting bndrun files in any sub-project in terminal, like:
```bash
[INFO] --- bnd-export-maven-plugin:5.1.2:export (default) @ eu.brain.iot.single-framework-example ---
[ERROR] Error   : Fail on changes set to true (--xchange,-x) and there are changes
[ERROR] Error   :    Existing runbundles   [com.fasterxml.jackson.core.jackson-annotations;version='[2.10.0,2.10.1)']
......]
[ERROR] Error   :    Calculated runbundles [com.fasterxml.jackson.core.jackson-annotations;version='[2.10.0,2.10.1)', ......]
```
You need to copy all printed auto-calculated bundles in brackets into the -runbundles instruction in the RobotBehaviour-Door.bndrun. Then build the repository again.

For further errors, welcome to let me know


## Shared tables
The four tables in [wiki](https://wiki.repository-pert.ismb.it/xwiki-enterprise-web-7.4.5/wiki/brainiot/view/Main/WP3+IoT+Framework+for+smart+dynamic+behaviour+%5BLeader%3A+ISMB/WP3_Activity_2020/) managed by warehouse backend has been updated using the coordinates in simulation, it's waiting for the confirmation from **Robotnik**. Generally the layout of the points is shown in Stage GUI:

![image](./Tables_Info_in_StageGUI.png)




*Welcome to review and give comments*

