package connector;

import task.*;


import org.lib.Command;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorMethodName {

	String name();
}
