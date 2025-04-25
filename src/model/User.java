package model;

import java.util.*;


public class User {
	private String emaile ;
	private String motdepass;
	private String name;
	private String prenome;
	private int nbrOfTasks = 1;
	private int nbrOfTasksDone = 1;
	private int nbrOfTasksFiled =0;
	private int nbrOfTasks_ThisDay;
	private int nbrOfTasksDone_ThisDay ;
	private int nbrOfTasksFiled_ThisDay ;
	
	//construvter pour la premier fois 
	
	public User(String emaile,String motdepass,String name,String prenome) {
		this.emaile =emaile;
		this.motdepass = motdepass;
		this.name = name;
		this.prenome=prenome;
		this.nbrOfTasks=1;
		this.nbrOfTasksDone=1;
		this.nbrOfTasksFiled =0;
		this.nbrOfTasksFiled_ThisDay=0;
		this.nbrOfTasks_ThisDay=1;
		this.nbrOfTasksDone_ThisDay =1;
		
		
	}
	
	

LinkedList<Task2> listoftasks = new LinkedList <Task2> ();
LinkedList<Task2> historiquelist = new LinkedList <Task2> ();
LinkedList<Task2> corbielist = new LinkedList <Task2> ();
	
	
	
	
	
	
	
	
}
