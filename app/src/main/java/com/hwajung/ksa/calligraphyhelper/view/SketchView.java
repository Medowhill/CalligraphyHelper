package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.activity.SketchActivity;
import com.hwajung.ksa.calligraphyhelper.object.Letter;
import com.hwajung.ksa.calligraphyhelper.object.Point;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Jaemin on 2015-04-25.
 */
public class SketchView extends View {
    // View to show main sketch screen

    private final int TOUCH_LETTER = 0, TOUCH_DELETE = 1, TOUCH_EDIT = 2, NO_TOUCH = 3;

    // Activity which calls this view
    private SketchActivity sketchActivity;

    // Paint
    // Letter paint, selected letter border paint, x, y axis paint, grid paint
    private Paint paint_selected, paint_axis, paint_line;

    // List of pointer touching the screen
    private ArrayList<Pointer> pointers;

    // Selected letter
    private int selectedLetter = -1;

    // List of letter
    private ArrayList<Letter> letters;

    // Stack saving change of letter for undo and redo
    private Stack<LetterChange> redoStack, undoStack;
    private LetterChange templateLetterData;

    // x, y direction movement of canvas
    private float xShift, yShift;

    // Magnification of canvas
    private float scale = 1;

    // Width and height of screen, pivot point of extension, reduction
    private int screenWidth, screenHeight, xPivot, yPivot;

    // Interval of grid
    private int interval = 160;

    // Bitmap for button
    private Bitmap bitmapDelete, bitmapEdit;

    public SketchView(Context context, AttributeSet attrs) {
        // Constructor
        super(context, attrs);

        Log.i("TEST", "KSA");

        // Give context to Letter class
        Letter.setResources(getContext());

        // Paint instance
        paint_selected = new Paint();
        paint_selected.setColor(getResources().getColor(R.color.sketch_selected));
        paint_selected.setStyle(Paint.Style.STROKE);

        paint_axis = new Paint();
        paint_axis.setColor(getResources().getColor(R.color.sketch_axis));

        paint_line = new Paint();
        paint_line.setColor(getResources().getColor(R.color.sketch_line));

        // Bitmap instance
        bitmapDelete = BitmapFactory.decodeResource(getResources(), R.drawable.sketch_button_delete);
        bitmapEdit = BitmapFactory.decodeResource(getResources(), R.drawable.sketch_button_edit);

        // Letter, pointer list initialize
        letters = new ArrayList<>();
        pointers = new ArrayList<>();

        // Stack initialize
        redoStack = new Stack<>();
        undoStack = new Stack<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Screen width, height, pivot point re-specification when the screen size change
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;
        xPivot = screenWidth / 2;
        yPivot = screenHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Fill canvas background in white
        canvas.drawColor(Color.WHITE);

        // Canvas extension, reduction, rotation
        canvas.scale(scale, scale, xPivot, yPivot);
        canvas.translate(xShift, yShift);

        // Pivot point of x,y axis
        Point leftTop = new Point(0, 0).transform(scale, xPivot, yPivot, xShift, yShift);
        Point rightBottom = new Point(screenWidth, screenHeight).transform(scale, xPivot, yPivot, xShift, yShift);

        // Change stroke width of paint for x,y axis
        paint_axis.setStrokeWidth(getResources().getInteger(R.integer.sketch_axis_stroke) / scale);

        // Draw x,y axis
        canvas.drawLine(0, leftTop.y, 0, rightBottom.y, paint_axis);
        canvas.drawLine(leftTop.x, 0, rightBottom.x, 0, paint_axis);

        // Change stroke width of paint for grid
        paint_line.setStrokeWidth(getResources().getInteger(R.integer.sketch_line_stroke) / scale);

        // Draw grid
        for (int i = (int) (leftTop.x / interval) - 1; i < (int) (rightBottom.x / interval) + 1; i++)
            if (i != 0)
                canvas.drawLine(i * interval, leftTop.y, i * interval, rightBottom.y, paint_line);
        for (int i = (int) (leftTop.y / interval) - 1; i < (int) (rightBottom.y / interval) + 1; i++)
            if (i != 0)
                canvas.drawLine(leftTop.x, i * interval, rightBottom.x, i * interval, paint_line);

        for (int i = 0; i < letters.size(); i++) {
            // Draw letter

            // Letter instance to draw
            Letter letter = letters.get(i);

            // Letter's location
            float x = letter.getPoint().x;
            float y = letter.getPoint().y;

            // Adjust canvas fit to letter's scale and degree
            canvas.scale(letter.getSize(), letter.getSize(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);
            canvas.rotate(letter.getDegree(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);

            // Draw letter bitmap
            canvas.drawBitmap(letter.getBitmap(), x, y, null);

            // Draw border if letter is selected
            if (i == selectedLetter) {
                paint_selected.setStrokeWidth(getResources().getInteger(R.integer.sketch_selected_stroke) / letter.getSize() / scale);
                canvas.drawRect(x, y, x + letter.getWidth(), y + letter.getHeight(), paint_selected);

                // Draw delete button
                Bitmap bitmap = Bitmap.createScaledBitmap(bitmapDelete,
                        (int) (bitmapDelete.getWidth() / scale / letter.getSize()),
                        (int) (bitmapDelete.getHeight() / scale / letter.getSize()), false);
                canvas.drawBitmap(bitmap, x + letter.getWidth() - bitmap.getWidth(), y, null);

                // Draw edit button
                bitmap = Bitmap.createScaledBitmap(bitmapEdit,
                        (int) (bitmapEdit.getWidth() / scale / letter.getSize()),
                        (int) (bitmapEdit.getHeight() / scale / letter.getSize()), false);
                canvas.drawBitmap(bitmap, x + letter.getWidth() - bitmap.getWidth(), y + letter.getHeight() - bitmap.getHeight(), null);
            }

            // Turn back canvas to origin state
            canvas.rotate(-letter.getDegree(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);
            canvas.scale(1.f / letter.getSize(), 1.f / letter.getSize(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Make activity's menu invisible
        if (sketchActivity != null)
            sketchActivity.closeMenu();

        // Location and id of event
        final float x = event.getX();
        final float y = event.getY();
        final int id = event.getPointerId(event.getActionIndex());

        // absolute coordinate about screen and relative coordinate about canvas
        Point point = new Point(x, y);
        Point trans = point.transform(scale, xPivot, yPivot, xShift, yShift);
        Point prevTrans;

        Pointer pointer, pointer0, pointer1;
        Letter letter;

        switch (event.getActionMasked()) {

            // Primary-pointer down
            case MotionEvent.ACTION_DOWN:
                // Make new pointer instance and add to list
                pointer = new Pointer(id, new Point(x, y));
                pointers.add(pointer);

                if (selectedLetter != -1) {
                    // If selected letter exists
                    letter = letters.get(selectedLetter);
                    if (touchLetter(trans, letter) != NO_TOUCH)
                        // If touch selected letter, specify pointer to move the letter
                        pointer.role = Pointer.ROLE_LETTER_MOVE;

                    // Save original letter data
                    templateLetterData = new LetterChange(selectedLetter, letter.getPoint(), letter.getSize(), letter.getDegree());
                }

                // Set cannot undo or redo while touching
                if (sketchActivity != null) {
                    sketchActivity.setRedoButton(false);
                    sketchActivity.setUndoButton(false);
                }

                break;

            // Non-primary-pointer down
            case MotionEvent.ACTION_POINTER_DOWN:
                // Remove primary-pointer role
                pointers.get(0).role = Pointer.ROLE_NONE;

                // Make new pointer instance and add to list
                pointer = new Pointer(id, new Point(x, y));
                pointer.role = Pointer.ROLE_NONE;
                pointers.add(pointer);

                break;

            // Primary-pointer up
            case MotionEvent.ACTION_UP:
                // Remove pointer from the list
                pointer = findPointer(id);
                pointers.remove(pointer);

                if (pointer.moved <= Pointer.MOVE_LIMIT) {
                    if (pointer.role == Pointer.ROLE_UNCERTAIN) {
                        // If pointer does not move and specified, check about role for letter selection
                        boolean b = true;
                        for (int i = 0; i < letters.size(); i++) {
                            letter = letters.get(i);
                            if (touchLetter(trans, letter) != NO_TOUCH) {
                                // If pointer touch letter, select letter
                                selectedLetter = i;
                                b = false;
                            }
                        }
                        if (b)
                            // If pointer does not touch any letter, delete selected letter
                            selectedLetter = -1;
                    } else if (pointer.role == Pointer.ROLE_LETTER_MOVE) {
                        letter = letters.get(selectedLetter);
                        switch (touchLetter(trans, letter)) {
                            case TOUCH_DELETE:
                                letters.remove(letter);
                                templateLetterData = new LetterChange(selectedLetter, letter.getId(),
                                        letter.getPoint(), letter.getSize(), letter.getDegree());
                                selectedLetter = -1;
                                break;
                            case TOUCH_EDIT:
                                if (sketchActivity != null)
                                    sketchActivity.showLetterEditDialog(letter.getColor(), letter.getSize(), letter.getDegree());
                                break;
                        }
                    }
                }

                // If touching is end, undo and redo is available
                if (sketchActivity != null) {
                    sketchActivity.setRedoButton(!redoStack.empty());
                    sketchActivity.setUndoButton(!undoStack.empty());
                }

                if (!(pointer.role == Pointer.ROLE_CANVAS_MOVE || pointer.role == Pointer.ROLE_UNCERTAIN)) {
                    // If letter changed, save original data on stack
                    undoStack.push(templateLetterData);
                    redoStack.clear();
                    if (sketchActivity != null) {
                        sketchActivity.setRedoButton(false);
                        sketchActivity.setUndoButton(true);
                    }
                }

                break;

            // Non-primary-pointer up
            case MotionEvent.ACTION_POINTER_UP:
                // Remove pointer from the list
                pointer = findPointer(id);
                pointers.remove(pointer);

                break;

            // Pointer move
            case MotionEvent.ACTION_MOVE:

                if (event.getPointerCount() == 1) {
                    // If pointer is one, canvas or letter translate

                    // Find pointer instance and record movement
                    pointer = findPointer(id);
                    pointer.moved++;

                    // If pointer does not specified and move enough, specify pointer to move canvas or letter
                    if (pointer.role == Pointer.ROLE_UNCERTAIN && pointer.moved > Pointer.MOVE_LIMIT)
                        pointer.role = Pointer.ROLE_CANVAS_MOVE;

                    // Pointer's previous coordinate's relative coordinate
                    prevTrans = pointer.prev.transform(scale, xPivot, yPivot, xShift, yShift);

                    switch (pointer.role) {
                        case Pointer.ROLE_LETTER_MOVE:
                            // If pointer is for letter's translation, change letter's coordinate
                            letter = letters.get(selectedLetter);
                            letter.setPoint(new Point(letter.getPoint().x + trans.x - prevTrans.x,
                                    letter.getPoint().y + trans.y - prevTrans.y));
                            break;
                        case Pointer.ROLE_CANVAS_MOVE:
                            // If pointer is for canvas's translation, change canvas's coordinate
                            xShift += trans.x - prevTrans.x;
                            yShift += trans.y - prevTrans.y;
                            break;
                    }

                    pointer.prev = point;
                } else {
                    // If pointers are more than 2, extend, reduce, and rotate by first 2 pointer
                    pointer0 = pointers.get(0);
                    pointer1 = pointers.get(1);

                    // Distance and degree of coordinates before movement
                    float prevDistance = Point.distance(pointer0.prev, pointer1.prev);
                    float prevDegree = Point.degree(pointer0.prev, pointer1.prev);

                    // Record movement
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        pointers.get(i).prev = new Point(event.getX(i), event.getY(i));
                    }

                    // Distance and degree of coordinates after movement
                    float distance = Point.distance(pointer0.prev, pointer1.prev);
                    float degree = Point.degree(pointer0.prev, pointer1.prev);

                    // Act only the values before movement are meaningful
                    if (prevDistance != 0 && distance != 0) {
                        if (selectedLetter == -1) {
                            // If letter is not selected, change scale of canvas
                            scale *= distance / prevDistance;
                        } else {
                            // If letter is selected, change scale and degree of letter
                            letter = letters.get(selectedLetter);
                            letter.setSize(letter.getSize() * distance / prevDistance);
                            letter.setDegree(letter.getDegree() + degree - prevDegree);
                        }
                    }
                }

                break;
        }

        invalidate();

        return true;
        // return super.onTouchEvent(event);
    }

    private Pointer findPointer(int id) {
        // Find pointer during pointer list by id

        for (Pointer pointer : pointers)
            if (pointer.id == id)
                return pointer;
        return null;
    }

    private int touchLetter(Point point, Letter letter) {
        // Check whether pointer touch letter or not

        float x = point.x, y = point.y;
        float x1 = x - letter.getPoint().x - letter.getWidth() / 2;
        x1 /= letter.getSize();
        float y1 = y - letter.getPoint().y - letter.getHeight() / 2;
        y1 /= letter.getSize();

        double x2 = x1 * Math.cos(-letter.getDegree() / 180 * Math.PI) - y1 * Math.sin(-letter.getDegree() / 180 * Math.PI);
        double y2 = x1 * Math.sin(-letter.getDegree() / 180 * Math.PI) + y1 * Math.cos(-letter.getDegree() / 180 * Math.PI);

        float width = letter.getWidth() / 2, height = letter.getHeight() / 2;

        if (width - bitmapDelete.getWidth() < x2 && x2 < width && -height < y2 && y2 < -height + bitmapDelete.getHeight())
            return TOUCH_DELETE;

        if (width - bitmapDelete.getWidth() < x2 && x2 < width && height - bitmapDelete.getHeight() < y2 && y2 < height)
            return TOUCH_EDIT;

        if (-width < x2 && x2 < width && -height < y2 && y2 < height)
            return TOUCH_LETTER;

        return NO_TOUCH;
    }

    private void unReDo(Stack<LetterChange> popStack, Stack<LetterChange> pushStack) {
        if (!popStack.empty()) {
            // If pop stack is not empty

            // Make new letter list by data from undo stack
            LetterChange letterData = popStack.pop();
            LetterChange data = null;
            Letter letter;

            switch (letterData.change) {
                case LetterChange.CHANGE_LETTER:
                    letter = letters.get(letterData.index);
                    data = new LetterChange(letterData.index, letter.getPoint(), letter.getSize(), letter.getDegree());

                    letter.setPoint(new Point(letterData.point.x, letterData.point.y));
                    letter.setDegree(letterData.degree);
                    letter.setSize(letterData.size);
                    break;
                case LetterChange.CHANGE_LETTER_ADDITION:
                    letter = letters.get(letterData.index);
                    data = new LetterChange(letterData.index, letter.getId(), letter.getPoint(),
                            letter.getSize(), letter.getDegree());
                    letters.remove(letterData.index);
                    if (selectedLetter == letterData.index)
                        selectedLetter = -1;
                    break;
                case LetterChange.CHANGE_LETTER_REMOVE:
                    letter = Letter.getLetter(letterData.id, letterData.point);
                    letter.setSize(letterData.size);
                    letter.setDegree(letterData.degree);
                    letters.add(letterData.index, letter);
                    data = new LetterChange(letterData.index);
                    break;
            }

            pushStack.push(data);

            // Set undo and redo button
            if (sketchActivity != null) {
                sketchActivity.setUndoButton(!undoStack.empty());
                sketchActivity.setRedoButton(!redoStack.empty());
            }

            invalidate();
        }
    }

    public void undo() {
        unReDo(undoStack, redoStack);
    }

    public void redo() {
        unReDo(redoStack, undoStack);
    }

    public void setSketchActivity(SketchActivity sketchActivity) {
        this.sketchActivity = sketchActivity;
    }

    public void addLetter(int id) {
        Letter letter = Letter.getLetter(id, new Point(0, 0));
        if (letter != null) {
            // Save original state at undo stack
            templateLetterData = new LetterChange(letters.size());
            undoStack.push(templateLetterData);
            redoStack.clear();

            letter.setPoint(new Point(-xShift + screenWidth / 2 / scale - letter.getWidth() / 2, -yShift + screenHeight / 2 / scale - letter.getHeight() / 2));
            letters.add(letter);
            selectedLetter = letters.size() - 1;
            invalidate();
        } else
            Toast.makeText(getContext(), "메모리가 부족하여 새로운 글씨 이미지를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
    }

    public byte[] getData() {
        String data = "";
        for (Letter letter : letters)
            data += 0 + "\t" + letter.getId() + "\t" + letter.getPoint().x + "\t" + letter.getPoint().y + "\t" + letter.getSize() + "\t" + letter.getDegree() + "\n";
        return data.getBytes();
    }

    public void setDataByByteArray(byte[] data) {
        letters.clear();

        String stringData = new String(data);
        String[] datas = stringData.split("\n");
        for (String str : datas) {
            String[] split = str.split("\t");
            switch (Integer.parseInt(split[0])) {
                case 0:
                    int id = Integer.parseInt(split[1]);
                    float x = Float.parseFloat(split[2]);
                    float y = Float.parseFloat(split[3]);
                    float size = Float.parseFloat(split[4]);
                    float degree = Float.parseFloat(split[5]);
                    Letter letter = Letter.getLetter(id, new Point(x, y));
                    letter.setSize(size);
                    letter.setDegree(degree);
                    letters.add(letter);
                    break;
            }
        }

        invalidate();
    }

    public void modifyLetter(int color, float size, float degree) {
        Letter letter = letters.get(selectedLetter);
        letter.setColor(color);
        letter.setSize(size);
        letter.setDegree(degree);
        invalidate();
    }

    // Pointer class
    private class Pointer {

        // constant for role
        final static int ROLE_UNCERTAIN = -1, ROLE_CANVAS_MOVE = 0, ROLE_LETTER_MOVE = 1, ROLE_NONE = 2;

        // Minimum movement to be pointer for translation
        final static int MOVE_LIMIT = 2;

        // Id of pointer
        int id;

        // Role of pointer, default is uncertain
        int role = ROLE_UNCERTAIN;

        // Count for movement of pointer
        int moved = 0;

        // Previous coordinate of pointer before movement
        Point prev;

        // Constructor
        Pointer(int id, Point prev) {
            this.id = id;
            this.prev = prev;
        }
    }

    private class LetterChange {

        final static int CHANGE_LETTER = 0, CHANGE_LETTER_ADDITION = 1, CHANGE_LETTER_REMOVE = 2;

        int change, index, id;
        Point point;
        float size, degree;

        LetterChange(int index, Point point, float size, float degree) {
            change = CHANGE_LETTER;
            this.index = index;
            this.point = new Point(point.x, point.y);
            this.size = size;
            this.degree = degree;
        }

        LetterChange(int index) {
            change = CHANGE_LETTER_ADDITION;
            this.index = index;
        }

        LetterChange(int index, int id, Point point, float size, float degree) {
            change = CHANGE_LETTER_REMOVE;
            this.index = index;
            this.id = id;
            this.point = new Point(point.x, point.y);
            this.size = size;
            this.degree = degree;
        }
    }
}
