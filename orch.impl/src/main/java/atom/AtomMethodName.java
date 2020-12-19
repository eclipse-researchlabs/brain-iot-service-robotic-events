package atom;

import task.*;

import port.*;

import org.lib.Command;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@Retention(RetentionPolicy.RUNTIME)
public @interface AtomMethodName {
	
	String name();
}
