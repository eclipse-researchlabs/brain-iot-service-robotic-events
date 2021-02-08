/*******************************************************************************
 * Copyright (C) 2021 LINKS Foundation
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package eu.brain.iot.robot.tables.queryer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.function.Predicate;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.brain.iot.eventing.annotation.SmartBehaviourDefinition;
import eu.brain.iot.eventing.api.BrainIoTEvent;
import eu.brain.iot.eventing.api.EventBus;
import eu.brain.iot.eventing.api.SmartBehaviour;
import eu.brain.iot.warehouse.events.CartMovedNotice;
import eu.brain.iot.warehouse.events.CartNoticeResponse;
import eu.brain.iot.warehouse.events.DockingRequest;
import eu.brain.iot.warehouse.events.DockingResponse;
import eu.brain.iot.warehouse.events.NewPickPointRequest;
import eu.brain.iot.warehouse.events.NewPickPointResponse;
import eu.brain.iot.warehouse.events.NewStoragePointRequest;
import eu.brain.iot.warehouse.events.NewStoragePointResponse;
import eu.brain.iot.warehouse.events.NoCartNotice;
import eu.brain.iot.robot.api.Coordinate;
import eu.brain.iot.robot.tables.creator.api.GetPickingTable;
import eu.brain.iot.robot.tables.creator.api.PickingTableValues;
import eu.brain.iot.robot.tables.creator.api.QueryDockResponse;
import eu.brain.iot.robot.tables.creator.api.QueryDockTable;
import eu.brain.iot.robot.tables.creator.api.QueryPickResponse;
import eu.brain.iot.robot.tables.creator.api.QueryPickingTable;
import eu.brain.iot.robot.tables.creator.api.QueryStorageResponse;
import eu.brain.iot.robot.tables.creator.api.QueryStorageTable;
import eu.brain.iot.robot.tables.creator.api.TableCreator;
import eu.brain.iot.robot.tables.creator.api.UpdatePickingTable;


@Component(service = { TableQueryer.class },
		   immediate = true,
		   configurationPid = "eu.brain.iot.robot.tables.queryer.TablesQueryer", 
		   configurationPolicy = ConfigurationPolicy.OPTIONAL
		)
@SmartBehaviourDefinition(
		consumed = { NewPickPointRequest.class, NewStoragePointRequest.class, NoCartNotice.class,
		CartMovedNotice.class, DockingRequest.class, QueryPickResponse.class, QueryStorageResponse.class, QueryDockResponse.class, PickingTableValues.class }, 
		author = "LINKS", name = "Warehouse Module: Tables Queryer", 
		description = "Implements the Tables Queryer.")

public class TableQueryer implements SmartBehaviour<BrainIoTEvent> { // TODO must able to cache multiple requests

	private ExecutorService worker;
	private ServiceRegistration<?> reg;
	
	@Reference
	private EventBus eventBus;
	
	private  Logger logger;
	private String logPath;
	
	@ObjectClassDefinition
	public static @interface Config {  // if run with creator, this logback.xml is not used
		String logPath() default "/opt/fabric/resources/logback.xml";

	}

	@Activate
	public void activate(BundleContext context, Config config, Map<String, Object> props) throws SQLException {
		this.logPath = config.logPath();

			System.setProperty("logback.configurationFile", logPath);
			
			logger = (Logger) LoggerFactory.getLogger(TableQueryer.class.getSimpleName());
					
			logger.info("Hello, this is Table Queryer ! UUID = "+ context.getProperty("org.osgi.framework.uuid"));
			System.out.println("Hello, this is Table Queryer !");
			
			logger.info("Table Queryer is using log: "+logPath);

			worker = Executors.newFixedThreadPool(10);
			
			Dictionary<String, Object> serviceProps = new Hashtable<>(props.entrySet().stream()
					.filter(e -> !e.getKey().startsWith(".")).collect(Collectors.toMap(Entry::getKey, Entry::getValue)));
			
			serviceProps.put(SmartBehaviourDefinition.PREFIX_ + "filter",  // get all events
					String.format("(robotID=*)"));
			
			logger.info("+++++++++ Table Queryer filter = " + serviceProps.get(SmartBehaviourDefinition.PREFIX_ + "filter"));
			reg = context.registerService(SmartBehaviour.class, this, serviceProps);
			
			logger.info("------------Queryer:  PickingTable ----------------");
			System.out.println("------------Queryer:  PickingTable ----------------");
			
			GetPickingTable get = new GetPickingTable();
			get.robotID = 0;
			eventBus.deliver(get);

	}
	
	
	@Override
	public void notify(BrainIoTEvent event) {  

		logger.info("--> Table Queryer received an event "+event.getClass());
		
		if (event instanceof NewPickPointRequest) { // TODO
			NewPickPointRequest pickRequest = (NewPickPointRequest) event;
			QueryPickingTable query = new QueryPickingTable();  // isAssigned = false
			query.isAssigned = false;
			query.robotID = pickRequest.robotID;

				logger.info("Queryer  sent QueryPickingTable "+ query);
				eventBus.deliver(query);
		
		} else if (event instanceof NewStoragePointRequest) { // TODO
			NewStoragePointRequest storageRequest = (NewStoragePointRequest) event;
			worker.execute(() -> {
				QueryStorageTable query = new QueryStorageTable();
				query.markerID = storageRequest.markerID;
				query.robotID = storageRequest.robotID;

					logger.info("Queryer  sent QueryStorageTable "+ query);
					eventBus.deliver(query);
			});
			
		} else if (event instanceof DockingRequest) { // TODO
			DockingRequest dockRequest = (DockingRequest) event;
			worker.execute(() -> {
				QueryDockTable query =  new QueryDockTable();
				query.robotIP = dockRequest.robotIP;
				query.robotID = dockRequest.robotID;
				
					logger.info("Queryer  sent QueryDockTable "+ query);
					eventBus.deliver(query);
				
			});
			
		} else if (event instanceof CartMovedNotice) {
			CartMovedNotice cartMovedNotice = (CartMovedNotice) event;
			worker.execute(() -> {
				UpdatePickingTable update = new UpdatePickingTable();
				update.isAssigned = false;
				update.robotID = cartMovedNotice.robotID;
				update.pickPoint = cartMovedNotice.pickPoint;

					logger.info("Queryer  sent UpdatePickingTable "+ update);
					eventBus.deliver(update);
			});
			
		} else if (event instanceof NoCartNotice) {
			NoCartNotice noCartNotice = (NoCartNotice) event;
			worker.execute(() -> {
				UpdatePickingTable update = new UpdatePickingTable();
				update.isAssigned = false;
				update.robotID = noCartNotice.robotID;
				update.pickPoint = noCartNotice.pickPoint;

					logger.info("Queryer  sent UpdatePickingTable "+ update);
					eventBus.deliver(update);
			});
		} 
		
		
		
		
		else if (event instanceof QueryPickResponse) {  // TODO how to provide robotID ?
			QueryPickResponse resp = (QueryPickResponse) event;
			worker.execute(() -> {
				NewPickPointResponse rs = new NewPickPointResponse();
				rs.robotID = resp.robotID;
				
				if (resp.pickPoint != null) {
					rs.hasNewPoint = true;
					rs.pickPoint = resp.pickPoint;
				}
				logger.info("Queryer  sent NewPickPointResponse " + rs);
				eventBus.deliver(rs);
			});
				
		} else if (event instanceof QueryStorageResponse) {
			QueryStorageResponse resp = (QueryStorageResponse) event;
			worker.execute(() -> {
				NewStoragePointResponse rs = new NewStoragePointResponse();
				rs.robotID = resp.robotID;
				
				rs.markerID = resp.markerID;
				rs.hasNewPoint = resp.hasNewPoint;
				rs.storagePoint = resp.storagePoint;
				rs.storageAuxliaryPoint = resp.storageAuxliaryPoint;
					logger.info("Queryer  sent NewStoragePointResponse "+ rs);
					eventBus.deliver(rs);

			});
			
		} else if (event instanceof QueryDockResponse) {
			QueryDockResponse resp = (QueryDockResponse) event;
			worker.execute(() -> {
				DockingResponse rs = new DockingResponse();
				rs.robotID = resp.robotID;
				
				rs.hasNewPoint = resp.hasNewPoint;
				rs.dockingPoint = resp.dockingPoint;
				rs.dockAuxliaryPoint = resp.dockAuxliaryPoint;
					logger.info("Queryer  sent DockingResponse "+ rs);
					eventBus.deliver(rs);
			});
			
		} else if (event instanceof PickingTableValues) {
			PickingTableValues resp = (PickingTableValues) event;

			logger.info("Queryer  gets Picking Table: \n"+ resp.pickingTableValues);
			System.out.println("Queryer  gets Picking Table: \n"+ resp.pickingTableValues);
			
		}
	}

	
	@Deactivate
	void stop() {
		worker.shutdown();
		try {
			worker.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("\nCreator Exception: {}", e.toString());
		}
		logger.info("------------  Table Queryer is deactivated----------------");
	}


}
