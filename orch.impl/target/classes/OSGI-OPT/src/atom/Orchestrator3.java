package atom;

import org.lib.Command;
import port.*;

import type.*;

import task.*;

// atom definition;

@Task(time=1)
public class Orchestrator3 extends Atom { 
org.lib.Command Command = new org.lib.Command() ;
// data definition;

Type<Integer> POSITION= new Type<Integer> (0); 
Type<Integer> AVAILABILITY= new Type<Integer> (0); 
Type<Integer> MARKER= new Type<Integer> (0); 
Type<Integer> RB_MARKER= new Type<Integer> (0); 
Type<Integer> RC_MARKER= new Type<Integer> (0); 
Type<Integer> COMMAND= new Type<Integer> (0); 
Type<Integer> GOTO= new Type<Integer> (0); 
Type<Integer> GOTOADD= new Type<Integer> (0); 
Type<Integer> GOTOCANCEL= new Type<Integer> (0); 
Type<Integer> GOTOQUERYSTATE= new Type<Integer> (0); 
Type<Integer> PICK= new Type<Integer> (0); 
Type<Integer> PICKADD= new Type<Integer> (0); 
Type<Integer> PICKCANCEL= new Type<Integer> (0); 
Type<Integer> PICKQUERYSTATE= new Type<Integer> (0); 
Type<Integer> PLACE= new Type<Integer> (0); 
Type<Integer> PLACEADD= new Type<Integer> (0); 
Type<Integer> PLACECANCEL= new Type<Integer> (0); 
Type<Integer> PLACEQUERYSTATE= new Type<Integer> (0); 
Type<Integer> CHARGE= new Type<Integer> (0); 
Type<Integer> CHARGEADD= new Type<Integer> (0); 
Type<Integer> CHARGECANCEL= new Type<Integer> (0); 
Type<Integer> CHARGEQUERYSTATE= new Type<Integer> (0); 
Type<Integer> UNCHARGE= new Type<Integer> (0); 
Type<Integer> UNCHARGEADD= new Type<Integer> (0); 
Type<Integer> UNCHARGECANCEL= new Type<Integer> (0); 
Type<Integer> UNCHARGEQUERYSTATE= new Type<Integer> (0); 
Type<Integer> QUERYSTATE= new Type<Integer> (0); 
Type<Integer> RB_QUERYSTATE= new Type<Integer> (0); 
Type<Integer> RC_QUERYSTATE= new Type<Integer> (0); 
Type<Integer> DOCKING= new Type<Integer> (0); 
Type<Integer> DOOR= new Type<Integer> (0); 
Type<Integer> STORAGE= new Type<Integer> (0); 
Type<Integer> UNLOAD= new Type<Integer> (0); 
Type<Integer> CART= new Type<Integer> (0); 
Type<Integer> NBCART= new Type<Integer> (0); 
Type<Integer> NBROBOT= new Type<Integer> (0); 
Type<Integer> FINISHED= new Type<Integer> (0); 
Type<Integer> RUNNING= new Type<Integer> (0); 
Type<Integer> POSITIONSTATE= new Type<Integer> (0); 
Type<Integer> TRUE= new Type<Integer> (0); 
Type<Integer> FALSE= new Type<Integer> (0); 
Type<Integer> CPT= new Type<Integer> (0); 
Type<Integer> RB_CPT= new Type<Integer> (0); 
Type<Integer> PLACECENTER= new Type<Integer> (0); 
Type<Integer> PLACELEFT= new Type<Integer> (0); 
Type<Integer> PLACERIGHT= new Type<Integer> (0); 
Type<Integer> CART1= new Type<Integer> (0); 
Type<Integer> CART2= new Type<Integer> (0); 
Type<Integer> CART3= new Type<Integer> (0); 
Type<Integer> LASTOPERATION= new Type<Integer> (0); 
Type<Integer> LOAD= new Type<Integer> (0); 
Type<Integer> DOORORCART= new Type<Integer> (0); 
public Orchestrator3( Type<Integer> robot_id){this.robot_id=robot_id; 
start();
 }

Type<Integer> robot_id= new Type<Integer> (); 




Type<Boolean> initial =new Type<Boolean> (true);
Type<Boolean>START=new Type<Boolean> (false);
Type<Boolean>S0=new Type<Boolean> (false);
Type<Boolean>S1=new Type<Boolean> (false);
Type<Boolean>S2=new Type<Boolean> (false);
Type<Boolean>S3=new Type<Boolean> (false);
Type<Boolean>S4=new Type<Boolean> (false);
Type<Boolean>S5=new Type<Boolean> (false);
Type<Boolean>S6=new Type<Boolean> (false);
Type<Boolean>S7=new Type<Boolean> (false);
Type<Boolean>S8=new Type<Boolean> (false);
Type<Boolean>S9=new Type<Boolean> (false);
Type<Boolean>S10=new Type<Boolean> (false);
Type<Boolean>S11=new Type<Boolean> (false);
Type<Boolean>S12=new Type<Boolean> (false);
Type<Boolean>S13=new Type<Boolean> (false);
Type<Boolean>S14=new Type<Boolean> (false);
public ePort p0= new ePort ( ); 
public ePort p1= new ePort ( ); 
public ePort p2= new ePort ( ); 
public ePort p3= new ePort ( ); 
public ePort p4= new ePort ( ); 
public ePort p5= new ePort ( ); 
public ePort p6= new ePort ( ); 
public ePort p7= new ePort ( ); 
public ePort p8= new ePort ( ); 
public ePort p9= new ePort ( ); 
public ePort p10= new ePort ( ); 
public ePort p11= new ePort ( ); 
public ePort p12= new ePort ( ); 
public ePort p13= new ePort ( ); 
public ePort p14= new ePort ( ); 
public ePort p15= new ePort ( ); 
public ePort p16= new ePort ( ); 
public ePort p17= new ePort ( ); 
public ePort p18= new ePort ( ); 
public ePort p19= new ePort ( ); 
public ePort p20= new ePort ( ); 
public ePort p21= new ePort ( ); 
public ePort p22= new ePort ( ); 
public ePort p23= new ePort ( ); 
public ePort p24= new ePort ( ); 
public ePort p25= new ePort ( ); 
public ePort p26= new ePort ( ); 
public ePort p27= new ePort ( ); 

 @AtomMethodName(name="initial")
 public int initial(){
 int _r=0;
 if( initial.getVal() ==true  ){POSITION.setVal(0 );
AVAILABILITY.setVal(1 );
MARKER.setVal(2 );
GOTO.setVal(3 );
GOTOADD.setVal(4 );
GOTOCANCEL.setVal(5 );
GOTOQUERYSTATE.setVal(6 );
PICK.setVal(7 );
PICKADD.setVal(8 );
PICKCANCEL.setVal(9 );
PICKQUERYSTATE.setVal(10 );
PLACE.setVal(11 );
PLACEADD.setVal(12 );
PLACECANCEL.setVal(13 );
PLACEQUERYSTATE.setVal(14 );
CHARGE.setVal(15 );
CHARGEADD.setVal(16 );
CHARGECANCEL.setVal(17 );
CHARGEQUERYSTATE.setVal(18 );
UNCHARGE.setVal(19 );
UNCHARGEADD.setVal(20 );
UNCHARGECANCEL.setVal(21 );
UNCHARGEQUERYSTATE.setVal(22 );
DOCKING.setVal(0 );
DOOR.setVal(1 );
STORAGE.setVal(4 );
UNLOAD.setVal(5 );
CART.setVal(2 );
NBCART.setVal(4 );
NBROBOT.setVal(1 );
CPT.setVal(robot_id.getVal() );
FINISHED.setVal(1 );
RUNNING.setVal(0 );
TRUE.setVal(1 );
FALSE.setVal(0 );
PLACECENTER.setVal(1 );
PLACELEFT.setVal(2 );
PLACERIGHT.setVal(3 );
CART1.setVal(1 );
CART2.setVal(2 );
CART3.setVal(3 );
LASTOPERATION.setVal(3 );
MARKER.setVal(0 );
LOAD.setVal(0 );
RB_CPT.setVal(2 );
_r=1;
 // Activate next states 
START.setVal(true);
initial.setVal(false);
 
   } return _r;}
 @AtomMethodName(name="p1")  
 public int p1(){
 int _r=0;
 if(  START.getVal( )==true  && p1.isAvailable() == false  )
{
p1.setAvailable(true);  
}
 if( p1.isAvailable() ){
p1.setAvailable(false);

 // Deactivate previous states 
START.setVal(false);
 // Activate next states 
S0.setVal(true);
 
_r=1;
  p1.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p2")  
 public int p2(){
 int _r=0;
 if(  S0.getVal( )==true  && p2.isAvailable() == false  )
{
p2.setAvailable(true);  
}
 if( p2.isAvailable() ){
p2.setAvailable(false);
QUERYSTATE.setVal( Command.queryState( CPT.getVal() , PLACE.getVal() ) );
 Command.printState( CPT.getVal() , QUERYSTATE.getVal() );

 // Deactivate previous states 
S0.setVal(false);
 // Activate next states 
S1.setVal(true);
 
_r=1;
  p2.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p3")  
 public int p3(){
 int _r=0;
 if(  S1.getVal( )==true  && p3.isAvailable() == false  && (QUERYSTATE.getVal() == FINISHED.getVal() ) )
{
p3.setAvailable(true);  
}
 if( p3.isAvailable() ){
p3.setAvailable(false);
 Command.writeGOTO( CPT.getVal() , UNLOAD.getVal() );
 Command.writeOpenDoor( );

 // Deactivate previous states 
S1.setVal(false);
 // Activate next states 
S2.setVal(true);
 
_r=1;
  p3.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p4")  
 public int p4(){
 int _r=0;
 if(  S1.getVal( )==true  && p4.isAvailable() == false  && (QUERYSTATE.getVal() != FINISHED.getVal() ) )
{
p4.setAvailable(true);  
}
 if( p4.isAvailable() ){
p4.setAvailable(false);

 // Deactivate previous states 
S1.setVal(false);
 // Activate next states 
S0.setVal(true);
 
_r=1;
  p4.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p5")  
 public int p5(){
 int _r=0;
 if(  S2.getVal( )==true  && p5.isAvailable() == false  )
{
p5.setAvailable(true);  
}
 if( p5.isAvailable() ){
p5.setAvailable(false);
QUERYSTATE.setVal( Command.queryState( CPT.getVal() , GOTO.getVal() ) );
 Command.printState( CPT.getVal() , QUERYSTATE.getVal() );

 // Deactivate previous states 
S2.setVal(false);
 // Activate next states 
S11.setVal(true);
 
_r=1;
  p5.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p6")  
 public int p6(){
 int _r=0;
 if(  S11.getVal( )==true  && p6.isAvailable() == false  && (QUERYSTATE.getVal() == FINISHED.getVal() ) )
{
p6.setAvailable(true);  
}
 if( p6.isAvailable() ){
p6.setAvailable(false);
 Command.writeCloseDoor( );
DOORORCART.setVal(1 );
MARKER.setVal( Command.checkMarkers( CPT.getVal() , DOORORCART.getVal() ) );

 // Deactivate previous states 
S11.setVal(false);
 // Activate next states 
S10.setVal(true);
 
_r=1;
  p6.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p7")  
 public int p7(){
 int _r=0;
 if(  S11.getVal( )==true  && p7.isAvailable() == false  && (QUERYSTATE.getVal() != FINISHED.getVal() ) )
{
p7.setAvailable(true);  
}
 if( p7.isAvailable() ){
p7.setAvailable(false);

 // Deactivate previous states 
S11.setVal(false);
 // Activate next states 
S2.setVal(true);
 
_r=1;
  p7.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p8")  
 public int p8(){
 int _r=0;
 if(  S10.getVal( )==true  && p8.isAvailable() == false  && (MARKER.getVal() == 0 ) )
{
p8.setAvailable(true);  
}
 if( p8.isAvailable() ){
p8.setAvailable(false);
LOAD.setVal(0 );

 // Deactivate previous states 
S10.setVal(false);
 // Activate next states 
S8.setVal(true);
 
_r=1;
  p8.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p9")  
 public int p9(){
 int _r=0;
 if(  S10.getVal( )==true  && p9.isAvailable() == false  && (MARKER.getVal() != 0 ) )
{
p9.setAvailable(true);  
}
 if( p9.isAvailable() ){
p9.setAvailable(false);
LOAD.setVal(1 );

 // Deactivate previous states 
S10.setVal(false);
 // Activate next states 
S3.setVal(true);
 
_r=1;
  p9.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p10")  
 public int p10(){
 int _r=0;
 if(  S3.getVal( )==true  && p10.isAvailable() == false  )
{
p10.setAvailable(true);  
}
 if( p10.isAvailable() ){
p10.setAvailable(false);
 Command.pickCart( CPT.getVal() , CART2.getVal() );

 // Deactivate previous states 
S3.setVal(false);
 // Activate next states 
S4.setVal(true);
 
_r=1;
  p10.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p11")  
 public int p11(){
 int _r=0;
 if(  S4.getVal( )==true  && p11.isAvailable() == false  )
{
p11.setAvailable(true);  
}
 if( p11.isAvailable() ){
p11.setAvailable(false);
QUERYSTATE.setVal( Command.queryState( CPT.getVal() , PICK.getVal() ) );
 Command.printState( CPT.getVal() , QUERYSTATE.getVal() );

 // Deactivate previous states 
S4.setVal(false);
 // Activate next states 
S6.setVal(true);
 
_r=1;
  p11.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p12")  
 public int p12(){
 int _r=0;
 if(  S6.getVal( )==true  && p12.isAvailable() == false  && (QUERYSTATE.getVal() == FINISHED.getVal() ) )
{
p12.setAvailable(true);  
}
 if( p12.isAvailable() ){
p12.setAvailable(false);
 Command.writeGOTO( CPT.getVal() , PLACERIGHT.getVal() );
 Command.writeOpenDoor( );

 // Deactivate previous states 
S6.setVal(false);
 // Activate next states 
S7.setVal(true);
 
_r=1;
  p12.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p13")  
 public int p13(){
 int _r=0;
 if(  S6.getVal( )==true  && p13.isAvailable() == false  && (QUERYSTATE.getVal() != FINISHED.getVal() ) )
{
p13.setAvailable(true);  
}
 if( p13.isAvailable() ){
p13.setAvailable(false);

 // Deactivate previous states 
S6.setVal(false);
 // Activate next states 
S4.setVal(true);
 
_r=1;
  p13.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p14")  
 public int p14(){
 int _r=0;
 if(  S7.getVal( )==true  && p14.isAvailable() == false  )
{
p14.setAvailable(true);  
}
 if( p14.isAvailable() ){
p14.setAvailable(false);
QUERYSTATE.setVal( Command.queryState( CPT.getVal() , GOTO.getVal() ) );
 Command.printState( CPT.getVal() , QUERYSTATE.getVal() );

 // Deactivate previous states 
S7.setVal(false);
 // Activate next states 
S8.setVal(true);
 
_r=1;
  p14.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p15")  
 public int p15(){
 int _r=0;
 if(  S8.getVal( )==true  && p15.isAvailable() == false  && (LOAD.getVal() == 0 ) )
{
p15.setAvailable(true);  
}
 if( p15.isAvailable() ){
p15.setAvailable(false);
 Command.writeGOTO( CPT.getVal() , PLACERIGHT.getVal() );
 Command.writeOpenDoor( );

 // Deactivate previous states 
S8.setVal(false);
 // Activate next states 
S9.setVal(true);
 
_r=1;
  p15.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p16")  
 public int p16(){
 int _r=0;
 if(  S9.getVal( )==true  && p16.isAvailable() == false  && (LOAD.getVal() == 0 ) )
{
p16.setAvailable(true);  
}
 if( p16.isAvailable() ){
p16.setAvailable(false);
QUERYSTATE.setVal( Command.queryState( CPT.getVal() , GOTO.getVal() ) );
 Command.printState( CPT.getVal() , QUERYSTATE.getVal() );

 // Deactivate previous states 
S9.setVal(false);
 // Activate next states 
S12.setVal(true);
 
_r=1;
  p16.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p17")  
 public int p17(){
 int _r=0;
 if(  S12.getVal( )==true  && p17.isAvailable() == false  && (QUERYSTATE.getVal() == FINISHED.getVal() ) )
{
p17.setAvailable(true);  
}
 if( p17.isAvailable() ){
p17.setAvailable(false);
 Command.writeCloseDoor( );
CPT.setVal(CPT.getVal() + 1 );

 // Deactivate previous states 
S12.setVal(false);
 // Activate next states 
S13.setVal(true);
 
_r=1;
  p17.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p18")  
 public int p18(){
 int _r=0;
 if(  S12.getVal( )==true  && p18.isAvailable() == false  && (QUERYSTATE.getVal() != FINISHED.getVal() ) )
{
p18.setAvailable(true);  
}
 if( p18.isAvailable() ){
p18.setAvailable(false);

 // Deactivate previous states 
S12.setVal(false);
 // Activate next states 
S9.setVal(true);
 
_r=1;
  p18.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p19")  
 public int p19(){
 int _r=0;
 if(  S8.getVal( )==true  && p19.isAvailable() == false  && (QUERYSTATE.getVal() == FINISHED.getVal() && LOAD.getVal() != 0 ) )
{
p19.setAvailable(true);  
}
 if( p19.isAvailable() ){
p19.setAvailable(false);
 Command.placeCART( CPT.getVal() , CART2.getVal() );
 Command.writeCloseDoor( );
CPT.setVal(CPT.getVal() + 1 );

 // Deactivate previous states 
S8.setVal(false);
 // Activate next states 
S9.setVal(true);
 
_r=1;
  p19.conceal(); 
  }

 
 return _r;}


 @AtomMethodName(name="p20")  
 public int p20(){
 int _r=0;
 if(  S8.getVal( )==true  && p20.isAvailable() == false  && (QUERYSTATE.getVal() != FINISHED.getVal() ) )
{
p20.setAvailable(true);  
}
 if( p20.isAvailable() ){
p20.setAvailable(false);

 // Deactivate previous states 
S8.setVal(false);
 // Activate next states 
S7.setVal(true);
 
_r=1;
  p20.conceal(); 
  }

 
 return _r;}

}