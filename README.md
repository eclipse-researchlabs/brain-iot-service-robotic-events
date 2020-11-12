# Robotics-Events

This repository defines the Events in the `eu.brain.iot.robot.api` sub-project to be used in the updated Service Robotics use case. It contains the following packages:

* **eu.brain.iot.robot.events**  (contains events between robot behaviour and ROS Edge Node)
* **eu.brain.iot.warehouse.events**   (contains events between robot behaviour and Warehouse Manager)

This repository also contains the door.api, door.impl, single-framework-example projects updated based on the M18 version, to be used in updated use case later.

See [BRAIN-IoT Events Word file](https://istitutoboella-my.sharepoint.com/:w:/g/personal/pert-projects_ismb_it/EaXDJA-FWppKsPSoleB5INsBm7kAwY1yRTDb9p4A0NZdZQ?e=yTaiG6) the detailed explaination of events.

The latest Robot Behaviour Workflow, which is exported as a HTML file with multiple pages"

* **Page 1**: Robot Behaviour Workflow in simulation, without checking door status and PickCart event hsa cart marker ID field 
* **Page 2**: Robot Behaviour Workflow in real robot, checking door status and all PickCart event are same, except the robotID
* **Page 3**: Service Robotic Architecture with SensiNact
* **Page 4**: Sequence Diagram with simplified Pickup Mission Execution
* **Page 5**: (To be updated based on page 2) Sequence Diagram with complete Pickup Mission Execution steps, involving SensiNact, Warehouse Backend, Robot Beahviour, Ros Edge Node, Door Edge Node
* **Page 6**: (To be updated based on page 2) Copy of **Page 5**, but also appended with the `Go back to Docking Area` sequence Diagram
* **Page 7**: (To be updated based on page 2) Copy of **Page 5**, but also involing the Physical Robot, and the detailed `Pickup Mission Execution` steps between ROS Edge Node and Physical Robot are also presented in  Physical Robot


To see the different pages, download the `Robotic_Workflows_SIM_and_RealRobot_Sequence_Diagrams.html` file and open it in browser, in the left-top of the fist page, there is the options to switch between different pages.

![image](./options.png)

To edit the diagram, download the source file `Robotic_Workflows_SIM_and_RealRobot_Sequence_Diagrams.drawio`, and open it in the Google APP `Diagrams.net Desktop `.

*Welcome to review and give comments*

