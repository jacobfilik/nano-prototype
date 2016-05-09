package org.dawnsci.prototype.e4.nano.table;

import org.eclipse.dawnsci.analysis.api.dataset.Slice;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;

public class SliceEditingSupport extends EditingSupport {

	private TextCellEditor editor;
	private Shell sliderShell;
	private Slider slider;
	private Dimension dimension;
	private int[] minMax;
	private boolean sliding = false;
	
	public SliceEditingSupport(ColumnViewer viewer) {
		super(viewer);
		editor = new TextCellEditor((Composite) getViewer().getControl(), SWT.NONE);
		Control control = editor.getControl();
		editor.getControl().addListener(SWT.Activate, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setShowingSlider(true);
				
			}
		});
		
//		editor.setValidator(new ICellEditorValidator() {
//			
//			@Override
//			public String isValid(Object value) {
////				try {
////					 Integer.parseInt(value.toString());
////				} catch (Exception e) {
////					return "not an int";
////				}
//				return null;
//			}
//		});
		
		if (control instanceof Text) {
			Text t = (Text)control;
			
			t.addVerifyListener(new VerifyListener() {
				
				@Override
				public void verifyText(VerifyEvent e) {
					String s = e.text;
					char character = e.character;
					
					 //Validation for keys like Backspace, left arrow key, right arrow key and del keys
					if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT
							|| e.keyCode == SWT.ARROW_RIGHT
							|| e.keyCode == SWT.DEL || e.character == ':') {
						e.doit = true;
						return;
					}

					if (e.character == '\0') {
						e.doit = true;
						return;
					}
					
					if (!('0' <= e.character && e.character <= '9')){
						e.doit = false;
						return;
					}
				}
			});
		}
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		dimension = (Dimension)element;
		((Text)editor.getControl()).setText("25");
		((Text)editor.getControl()).setSelection(26);
		if (sliderShell!=null&&sliderShell.isVisible()) return editor;
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		dimension = (Dimension)element;
		minMax = new int[]{0,dimension.getSize()};
		if (slider != null) {
			Slice slice = dimension.getSlice();
			slider.setMinimum(0);
			int size = dimension.getSize();
			int start = slice.getStart() == null ? 0 : slice.getStart();
			int stop = slice.getStop() == null ? dimension.getSize()-1 : slice.getStop();
//			int test = dimension.getSize() - (slice.getStop() -slice.getStart());
			slider.setMaximum(1+dimension.getSize() - (stop -start));
			slider.setSelection(start);
			slider.setIncrement(1);
		}
		System.out.println("Get " + ((Dimension)element).getSlice().toString());
		return ((Dimension)element).getSlice().toString();
	}
	


	@Override
	protected void setValue(Object element, Object value) {
		if (value.toString().isEmpty())return;
		Slice[] s = Slice.convertFromString(value.toString());
		slider.setSelection(s[0].getStart());
		if (s == null) return;
		System.out.println(value.toString());
		((Dimension)element).setSlice(s[0]);
		getViewer().refresh();
		System.out.println("Get " + ((Dimension)element).getSlice().toString());

	}
	
	private void createSliderShell() {
		if (sliderShell != null) return;
		
		sliderShell = new Shell(this.getViewer().getControl().getShell(), SWT.NO_FOCUS | SWT.ON_TOP | SWT.TOOL);
		sliderShell.setLayout(new GridLayout(1, false));
		
		final Listener closeListener = new Listener() {
			@Override
			public void handleEvent(final Event e) {
				if (e.type == SWT.Traverse) setShowingSlider(false);
				if (sliding) return;
				if (slider.isFocusControl()) return;
				setShowingSlider(false);
			}
		};

		// Listeners on this popup's shell
		sliderShell.addListener(SWT.Deactivate, closeListener);
		sliderShell.addListener(SWT.Close, closeListener);
		sliderShell.addListener(SWT.FocusOut, closeListener);

		// Listeners on the target control
		editor.getControl().addListener(SWT.MouseDoubleClick, closeListener);
		editor.getControl().addListener(SWT.MouseDown, closeListener);
		editor.getControl().addListener(SWT.Dispose, closeListener);
		editor.getControl().addListener(SWT.FocusOut, closeListener);
		editor.getControl().addListener(SWT.Traverse, closeListener);
		// Listeners on the target control's shell
		Shell controlShell = editor.getControl().getShell();
		controlShell.addListener(SWT.Move, closeListener);
		controlShell.addListener(SWT.Resize, closeListener);


        slider = new Slider(sliderShell, SWT.None);		
        slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        slider.setValues(0, minMax[0], minMax[1], 1, 1, 10);
        slider.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				sliding = false;
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				sliding = true;
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
        slider.addSelectionListener(new SelectionAdapter() {
        	
        	public void widgetSelected(SelectionEvent e) {
//        		System.out.println(slider.getSelection());
        		slider.setFocus();
//        		Text control = (Text)editor.getControl();
        		Slice slice = dimension.getSlice();
        		Control focusControl = Display.getCurrent().getFocusControl();
//        		control.setText("000");
//        		control.redraw();
        		Slice s = dimension.getSlice();
        		int start = slice.getStart() == null ? 0 : slice.getStart();
    			int stop = slice.getStop() == null ? dimension.getSize()-1 : slice.getStop();
        		int dif = stop-start;
        		String val = Integer.toString((slider.getSelection()));
        		if (dif > 1) {
        			val = Integer.toString(slider.getSelection()) + ":" + Integer.toString((slider.getSelection()+dif));
        			slider.setMaximum(dimension.getSize()-dif);
        		}
        		editor.setValue(val);
        		setValue(dimension, val);
        		
        	}
		});
        
        slider.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) { }
			@Override
			public void focusLost(FocusEvent e) {
				setShowingSlider(false);
			}
        });
        
        sliderShell.pack();
//        shellCreated = true;
	}
	
	
	protected void setShowingSlider(final boolean isShow) {

		if (isShow) {
			createSliderShell();	
			Rectangle sizeT= editor.getControl().getBounds();
			Point     pntT = editor.getControl().toDisplay(-2, sizeT.height-4);
			Rectangle rect = new Rectangle(pntT.x, pntT.y, sizeT.width-2, sliderShell.getBounds().height);
			sliderShell.setBounds(rect);
			sliderShell.setVisible(true);
		} else {
			if (sliderShell!=null&&!sliderShell.isDisposed()) {
				sliderShell.setVisible(false);
			}
		}
	}
}

	

