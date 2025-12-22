package com.lms.admin.controller;



import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Vlayout;

public class UsersComposer extends SelectorComposer<Div>
{
	private static final long serialVersionUID = -6735214827680029146L;
	
	@Wire
	private Vlayout mainContainer;
	
	@Wire
	private Button amitbtn,rahulbtn,vikrambtn,snehabtn;

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
	
	@Listen("onClick = #amitbtn")
	 public void onAmitbtn(Event event) 
	{
		Button btn = (Button) event.getTarget();
	    String btnId = btn.getId();
		Executions.sendRedirect("userdetails.zul?btnId=" + btnId);
	}
	
	@Listen("onClick = #rahulbtn")
	 public void onRahulbtn(Event event) 
	{
		Button btn = (Button) event.getTarget();
	    String btnId = btn.getId();
		Executions.sendRedirect("userdetails.zul?btnId=" + btnId);
	}
	
	@Listen("onClick = #vikrambtn")
	 public void onVikrambtn(Event event) 
	{
		Button btn = (Button) event.getTarget();
	    String btnId = btn.getId();
		Executions.sendRedirect("userdetails.zul?btnId=" + btnId);
	}
	
	@Listen("onClick = #snehabtn")
	 public void onSnehabtn(Event event) 
	{
		Button btn = (Button) event.getTarget();
	    String btnId = btn.getId();
		Executions.sendRedirect("userdetails.zul?btnId=" + btnId);
	}
}