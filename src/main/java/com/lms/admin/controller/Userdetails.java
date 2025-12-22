package com.lms.admin.controller;



import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

public class Userdetails extends SelectorComposer<Div>
{
	private static final long serialVersionUID = -6735214827680029146L;
	
	@Wire
	private Vlayout mainContainer;
	
	@Wire
	private Hlayout amithlayout,rahulhlayout,vikramhlayout,snehahlayout;

	
	@Override
	public void doAfterCompose(Div comp) throws Exception
	{
		// TODO Auto-generated method stub
		super.doAfterCompose(comp);
		EventQueues.lookup("dashboardQueue", EventQueues.DESKTOP, true)
        .subscribe(event -> {
            if ("onSidebarToggle".equals(event.getName()))
            {
                resizeContent();
            }
        });
		
		String btnId = Executions.getCurrent().getParameter("btnId");

	    if(btnId.equalsIgnoreCase("amitbtn"))
	    {
	    	amithlayout.setVisible(true);
	    }
	    else if(btnId.equalsIgnoreCase("rahulbtn"))
	    {
	    	rahulhlayout.setVisible(true);
	    }
	    else if(btnId.equalsIgnoreCase("vikrambtn"))
	    {
	    	vikramhlayout.setVisible(true);
	    }
	    else if(btnId.equalsIgnoreCase("snehabtn"))
	    {
	    	snehahlayout.setVisible(true);
	    }
	    
	}
	
	private void resizeContent() 
	{
		if (mainContainer != null)
		{
            if (mainContainer.getSclass().contains("enlarge")) 
            {
                mainContainer.setSclass("main-container");
            }
            else
            {
                mainContainer.setSclass("main-container enlarge");
            }
        }
    }
}