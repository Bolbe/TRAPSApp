package com.traps.trapsapp.core;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.traps.trapsapp.PenaltyActivity;
import com.traps.trapsapp.R;

public class PenaltyPad implements DialogInterface.OnMultiChoiceClickListener, 
										DialogInterface.OnClickListener {

	private Button[][] penButton = new Button[SystemParam.MAX_GATE_PER_TERMINAL][3];
	private TextView[] penText = new TextView[SystemParam.MAX_GATE_PER_TERMINAL];
	private View[] rowView = new View[SystemParam.MAX_GATE_PER_TERMINAL];
	private Map<Button, PenaltyButton> buttonMap = new HashMap<Button, PenaltyButton>(); 
	 
	// map row index with the gate index
	private int[] gateIndex = new int[SystemParam.MAX_GATE_PER_TERMINAL];
	
	private int[] penalty = new int[SystemParam.MAX_GATE_PER_TERMINAL];
	// 0-25 true if the gate is displayed false otherwise
	private boolean[] gateSelection = new boolean[SystemParam.GATE_COUNT];
	
	private static String[] gateStringList = new String[SystemParam.GATE_COUNT];
	
	private boolean[] tmpGateSelection = new boolean[SystemParam.GATE_COUNT];
	
	private PenaltyActivity terminal;
	
	private int rowCount = 0;
	private TrapsDB db;


	static {
		
		for (int i=0; i<SystemParam.GATE_COUNT; i++) {
			if (i<9) gateStringList[i]=" 0"+(i+1)+" ";
			else gateStringList[i]=" "+(i+1)+" ";
		}
		
	}
	
	public int getRowCount() {
		return rowCount;
	}


	

	private void setGateSelection(boolean[] selectedGate) {
		
		int row = 0;
		resetGateSelection();

		for (int index=0; index<selectedGate.length; index++) {
			if (row==SystemParam.MAX_GATE_PER_TERMINAL) break;
			if (!selectedGate[index]) continue;
			gateSelection[index] = true;
			gateIndex[row] = index;
			rowView[row].setVisibility(View.VISIBLE);
			penText[row].setText(gateStringList[index]);
			resetPenalty(row);
			row++;
		}
		rowCount = row;
		for (; row<SystemParam.MAX_GATE_PER_TERMINAL; row++) {
			rowView[row].setVisibility(View.GONE);
			
		}
		
		// now store in the db
		db.setGateSelection(selectedGate);
		
	}
	
	public boolean[] getGateSelection() {
		return gateSelection;
	}

	// return true if no penalty is entered
	public boolean noPenalty() {
		for (int i=0; i<rowCount; i++) 
			if (penalty[i]>-1) return false;
		return true;
	}

	public int getPenalty(int gateIndex) {
		if ((gateIndex<0) || (gateIndex>=penalty.length)) return -1;
		return penalty[gateIndex];
		
	}
	
	public SparseIntArray getPenaltyMap() {
		SparseIntArray values = new SparseIntArray();
		int localIndex = 0;
		for (int index=0; index<gateSelection.length; index++) {
			if (gateSelection[index]==true) {
				// the gate index starts at 0
				values.put(index, getPenalty(localIndex++));
			}
		}
		return values;
	}
	
	private void resetPenalty(int gateIndex) {
		for (int i=0; i<3; i++) {
			penButton[gateIndex][i].setBackgroundResource(R.drawable.penalty);
			penButton[gateIndex][i].setTextColor(Color.LTGRAY);
			penButton[gateIndex][i].setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		}
		penalty[gateIndex] = -1;
	}

	/**
	 * Set the penalties stored in the map. If the map contains a gate number not assigned to this
	 * penalty pad, it is ignored
	 * @param map
	 */
	public void setPenaltyMap(SparseIntArray map) {
		for (int row=0; row<rowCount; row++) {
			Integer pen = map.get(gateIndex[row]);
			if (pen!=null) setPenalty(row, pen);
			
		}
	}
	
	public void setPenalty(int gateIndex, int value) {
		if ((gateIndex<0) || (gateIndex>=penalty.length)) return;
		resetPenalty(gateIndex);
		
		switch (value) {

			case 0: 
				
				penButton[gateIndex][0].setBackgroundResource(R.drawable.penalty0);
				penButton[gateIndex][0].setTextColor(Color.BLACK);
				penButton[gateIndex][0].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				penalty[gateIndex] = 0;
				
				break;
				
			case 2: 
				
				penButton[gateIndex][1].setBackgroundResource(R.drawable.penalty2);
				penButton[gateIndex][1].setTextColor(Color.BLACK);
				penButton[gateIndex][1].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				penalty[gateIndex] = 2;
				
				break;
				
			case 50: 
				
				penButton[gateIndex][2].setBackgroundResource(R.drawable.penalty50);
				penButton[gateIndex][2].setTextColor(Color.BLACK);
				penButton[gateIndex][2].setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				penalty[gateIndex] = 50;
			
				break;
				
				
		}
		
	}

	private void setOnClickListener() {
		
		
		for (View button: buttonMap.keySet()) {
			
			button.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	            	PenaltyButton penaltyButton = buttonMap.get(v);
	                setPenalty(penaltyButton.getGateIndex(), penaltyButton.getValue());
	                if (penaltyButton.getValue()==0) terminal.play(terminal.sndMidPitch);
	                else terminal.play(terminal.sndLowPitch);
	                
	                
	            }
	        });
		}
		
		

		
	}
	
	/**
	 * Build the map with the button views as key, and gateIndex, penatyIndex and penalty value as map values
	 */
	private void buildButtonMap() {
	
		for (int gateIndex=0; gateIndex<SystemParam.MAX_GATE_PER_TERMINAL; gateIndex++) {
			
			buttonMap.put(penButton[gateIndex][0], new PenaltyButton(gateIndex, 0, 0));
			buttonMap.put(penButton[gateIndex][1], new PenaltyButton(gateIndex, 1, 2));
			buttonMap.put(penButton[gateIndex][2], new PenaltyButton(gateIndex, 2, 50));
			
		}
		
	}
	
	/**
	 * Set all the gates to false (no gate selected)
	 */
	private void resetGateSelection() {
		for (int i=0; i<SystemParam.GATE_COUNT; i++) gateSelection[i] = false;
	}
	
	public PenaltyPad (PenaltyActivity terminal) {
		
		this.terminal = terminal;
		db = TrapsDB.getInstance();
		
		resetGateSelection();
		
		penButton[0][0] = (Button)terminal.findViewById(R.id.penalty00);	
		penButton[0][1] = (Button)terminal.findViewById(R.id.penalty01);
		penButton[0][2] = (Button)terminal.findViewById(R.id.penalty02);
		
		penButton[1][0] = (Button)terminal.findViewById(R.id.penalty10);
		penButton[1][1] = (Button)terminal.findViewById(R.id.penalty11);
		penButton[1][2] = (Button)terminal.findViewById(R.id.penalty12);
		
		penButton[2][0] = (Button)terminal.findViewById(R.id.penalty20);
		penButton[2][1] = (Button)terminal.findViewById(R.id.penalty21);
		penButton[2][2] = (Button)terminal.findViewById(R.id.penalty22);
		
		penButton[3][0] = (Button)terminal.findViewById(R.id.penalty30);
		penButton[3][1] = (Button)terminal.findViewById(R.id.penalty31);
		penButton[3][2] = (Button)terminal.findViewById(R.id.penalty32);
		
		penButton[4][0] = (Button)terminal.findViewById(R.id.penalty40);
		penButton[4][1] = (Button)terminal.findViewById(R.id.penalty41);
		penButton[4][2] = (Button)terminal.findViewById(R.id.penalty42);
		
		buildButtonMap();
		setOnClickListener();
		
		penText[0] = (TextView)terminal.findViewById(R.id.gateText0);
		penText[1] = (TextView)terminal.findViewById(R.id.gateText1);
		penText[2] = (TextView)terminal.findViewById(R.id.gateText2);
		penText[3] = (TextView)terminal.findViewById(R.id.gateText3);
		penText[4] = (TextView)terminal.findViewById(R.id.gateText4);
		
		rowView[0] = (View)terminal.findViewById(R.id.row0);
		rowView[1] = (View)terminal.findViewById(R.id.row1);
		rowView[2] = (View)terminal.findViewById(R.id.row2);
		rowView[3] = (View)terminal.findViewById(R.id.row3);
		rowView[4] = (View)terminal.findViewById(R.id.row4);

		setGateSelection(db.getGateSelection());
		
	}

	public AlertDialog getDialogGateSelection(Activity activity) {
	    	
	    	tmpGateSelection = gateSelection.clone();
	    	return new AlertDialog.Builder(activity)
	    		.setTitle("Choix des portes")
	    		.setMultiChoiceItems(gateStringList, tmpGateSelection,this)
	    		.setPositiveButton("OK",this)
	            .setNegativeButton("Annuler",this)
	    		.create();
	    	
	    }
	    

	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		
		tmpGateSelection[which] = isChecked;
		Log.d("PenaltyPad","Check button "+which+" = "+isChecked);
				
	}



	public void onClick(DialogInterface dialog, int which) {
		
		Log.d("PenaltyPad","Dialog button "+which+" pressed");
		if (which==-1) {
			int counter = 0;
			for (int i=0; i<tmpGateSelection.length; i++) {
				if (tmpGateSelection[i]==true) counter++;
			}
			if (counter>SystemParam.MAX_GATE_PER_TERMINAL) {
				new AlertDialog.Builder(terminal)
	    		.setTitle("Trop de portes sélectionnées (5 max)")
	    		.setNeutralButton("OK",null)
	    		.create()
	    		.show();
			}
			else if (counter==0) {
				new AlertDialog.Builder(terminal)
	    		.setTitle("Au moins une porte doit être sélectionnée")
	    		.setNeutralButton("OK",null)
	    		.create()
	    		.show();
			}
			else {
				setGateSelection(tmpGateSelection);
				
			}
		}
	};
	
	
}
