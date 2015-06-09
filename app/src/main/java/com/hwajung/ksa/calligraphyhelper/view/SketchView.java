package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.hwajung.ksa.calligraphyhelper.R;
import com.hwajung.ksa.calligraphyhelper.activity.SketchActivity;
import com.hwajung.ksa.calligraphyhelper.object.Letter;
import com.hwajung.ksa.calligraphyhelper.object.Point;

import java.util.ArrayList;
import java.util.Stack;

/**
 * (C) 2015. Jaemin Hong all rights reserved.
 */
public class SketchView extends View {
    // View to show main sketch screen

    private final int TOUCH_LETTER = 0, TOUCH_DELETE = 1, TOUCH_EDIT = 2, NO_TOUCH = 3;
    private final float MIN_X = 0, MAX_X = 10000, MIN_Y = 0, MAX_Y = 10000, MIN_SCALE = 0.2f, MAX_SCALE = 5;

    // Activity which calls this view
    private SketchActivity sketchActivity;

    // Paint
    // Letter paint, selected letter border paint, x, y axis paint, grid paint
    private Paint paint_selected, paint_line;
    private Paint paint_drawing;

    // List of pointer touching the screen
    private ArrayList<Pointer> pointers;

    // Selected letter
    private int selectedLetter = -1;

    // List of letter
    private ArrayList<Letter> letters;

    // Stack saving change of letter for undo and redo
    private Stack<LetterChange> redoStack, undoStack;
    private Stack<Stroke> strokeStack;
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

    private boolean drawingMode = false;

    private boolean drawGrid = true;
    private boolean drawLetter = true;

    private ArrayList<Point> drawingPoints;
    private ArrayList<Stroke> drawingStrokes;

    private boolean isCircle = true;
    private float thickness = 10;
    private float degree = 0;

    public SketchView(Context context, AttributeSet attrs) {
        // Constructor
        super(context, attrs);

        // Give context to Letter class
        Letter.setResources(getContext());

        // Paint instance
        paint_selected = new Paint();
        paint_selected.setColor(getResources().getColor(R.color.sketch_selected));
        paint_selected.setStyle(Paint.Style.STROKE);

        paint_line = new Paint();
        paint_line.setColor(getResources().getColor(R.color.sketch_line));

        paint_drawing = new Paint();
        paint_drawing.setColor(Color.BLACK);
        paint_drawing.setStrokeWidth(5);
        paint_drawing.setStyle(Paint.Style.FILL);

        // Bitmap instance
        bitmapDelete = BitmapFactory.decodeResource(getResources(), R.drawable.sketch_button_delete);
        bitmapEdit = BitmapFactory.decodeResource(getResources(), R.drawable.sketch_button_edit);

        // Letter, pointer list initialize
        letters = new ArrayList<>();
        pointers = new ArrayList<>();

        // Stack initialize
        redoStack = new Stack<>();
        undoStack = new Stack<>();

        // Stack initialize
        strokeStack = new Stack<>();
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

        // Canvas extension, reduction, translation
        canvas.scale(scale, scale, xPivot, yPivot);
        canvas.translate(xShift, yShift);

        if (drawGrid) {
            // Pivot point of grid
            Point leftTop = new Point(0, 0).transform(scale, xPivot, yPivot, xShift, yShift);
            Point rightBottom = new Point(screenWidth, screenHeight).transform(scale, xPivot, yPivot, xShift, yShift);

            // Change stroke width of paint for grid
            paint_line.setStrokeWidth(getResources().getInteger(R.integer.sketch_line_stroke) / scale);

            // Draw grid
            for (int i = (int) (leftTop.x / interval) - 1; i < (int) (rightBottom.x / interval) + 1; i++)
                canvas.drawLine(i * interval, leftTop.y, i * interval, rightBottom.y, paint_line);
            for (int i = (int) (leftTop.y / interval) - 1; i < (int) (rightBottom.y / interval) + 1; i++)
                canvas.drawLine(leftTop.x, i * interval, rightBottom.x, i * interval, paint_line);
        }

        if (drawLetter) {
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

        if (drawingMode) {
            paint_drawing.setStrokeWidth(thickness / scale * 2);

            for (int i = 1; i < drawingPoints.size(); i++) {
                Point p1 = drawingPoints.get(i - 1);
                Point p2 = drawingPoints.get(i);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint_drawing);
            }
            for (int i = 0; i < drawingStrokes.size(); i++)
                drawingStrokes.get(i).draw(canvas);
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

        if (drawingMode) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Set cannot undo or redo while touching
                    if (sketchActivity != null) {
                        sketchActivity.setRedoButton(false);
                        sketchActivity.setUndoButton(false);
                        sketchActivity.setAddButton(false);
                    }
                case MotionEvent.ACTION_MOVE:
                    drawingPoints.add(trans);
                    break;
                case MotionEvent.ACTION_UP:
                    drawingPoints.add(trans);
                    Path path = new Path();
                    path.moveTo(drawingPoints.get(0).x, drawingPoints.get(0).y);
                    for (int i = 0; i < drawingPoints.size() - 2; i++) {
                        float x1 = drawingPoints.get(i).x;
                        float y1 = drawingPoints.get(i).y;
                        float x2 = drawingPoints.get(i + 1).x;
                        float y2 = drawingPoints.get(i + 1).y;
                        float x3 = drawingPoints.get(i + 2).x;
                        float y3 = drawingPoints.get(i + 2).y;
                        float deg1 = (float) Math.atan((y2 - y1) / (x2 - x1));
                        float deg2 = (float) Math.atan((y3 - y2) / (x3 - x2));
                        path.rLineTo(x2 - x1, y2 - y1);
                        path.addArc(new RectF(x2 - thickness, y2 - thickness, x2 + thickness, y2 + thickness), (float) -Math.PI / 2 - deg1, deg1 - deg2);
                    }
                    drawingStrokes.add(new Stroke(isCircle));
                    drawingPoints.clear();

                    if (sketchActivity != null) {
                        strokeStack.clear();
                        sketchActivity.setRedoButton(false);
                        sketchActivity.setUndoButton(true);
                        sketchActivity.setAddButton(true);
                    }
                    break;
            }

        } else {
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
                        templateLetterData = new LetterChange(selectedLetter, letter.getPoint(),
                                letter.getSize(), letter.getDegree(), letter.getColor());
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
                                            letter.getPoint(), letter.getSize(), letter.getDegree(), letter.getColor());
                                    selectedLetter = -1;

                                    undoStack.push(templateLetterData);
                                    redoStack.clear();
                                    if (sketchActivity != null) {
                                        sketchActivity.setRedoButton(false);
                                        sketchActivity.setUndoButton(true);
                                        sketchActivity.modify();
                                    }
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

                    if (!(pointer.role == Pointer.ROLE_CANVAS_MOVE || pointer.role == Pointer.ROLE_UNCERTAIN) && selectedLetter != -1) {
                        // If letter changed, save original data on stack
                        undoStack.push(templateLetterData);
                        redoStack.clear();
                        if (sketchActivity != null) {
                            sketchActivity.setRedoButton(false);
                            sketchActivity.setUndoButton(true);
                            sketchActivity.modify();
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
                                regulate();
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
                                regulate();
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
        }

        invalidate();

        return true;
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

        float size = bitmapDelete.getWidth();

        if (width - size * 1.5f < x2 && x2 < width + size * 0.5f
                && -height - size * 0.5f < y2 && y2 < -height + size * 1.5f)
            return TOUCH_DELETE;

        if (width - size * 1.5f < x2 && x2 < width + size * 0.5f
                && height - size * 1.5f < y2 && y2 < height + size * 0.5f)
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
                    data = new LetterChange(letterData.index, letter.getPoint(), letter.getSize(),
                            letter.getDegree(), letter.getColor());

                    letter.setPoint(new Point(letterData.point.x, letterData.point.y));
                    letter.setDegree(letterData.degree);
                    letter.setSize(letterData.size);
                    letter.setColor(letterData.color);
                    letter.loadBitmap();
                    break;
                case LetterChange.CHANGE_LETTER_ADDITION:
                    letter = letters.get(letterData.index);
                    data = new LetterChange(letterData.index, letter.getId(), letter.getPoint(),
                            letter.getSize(), letter.getDegree(), letter.getColor());
                    letters.remove(letterData.index);
                    if (selectedLetter == letterData.index)
                        selectedLetter = -1;
                    break;
                case LetterChange.CHANGE_LETTER_REMOVE:
                    letter = new Letter(letterData.id, letterData.color);
                    letter.setPoint(new Point(letterData.point.x, letterData.point.y));
                    letter.setSize(letterData.size);
                    letter.setDegree(letterData.degree);
                    letter.loadBitmap();
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
        if (drawingMode) {
            if (!drawingStrokes.isEmpty()) {
                strokeStack.push(drawingStrokes.get(drawingStrokes.size() - 1));
                drawingStrokes.remove(drawingStrokes.size() - 1);

                if (sketchActivity != null) {
                    sketchActivity.setAddButton(!drawingStrokes.isEmpty());
                    sketchActivity.setUndoButton(!drawingStrokes.isEmpty());
                    sketchActivity.setRedoButton(true);
                }
                invalidate();
            }
        } else {
            if (sketchActivity != null)
                sketchActivity.modify();
            unReDo(undoStack, redoStack);
        }
    }

    public void redo() {
        if (drawingMode) {
            if (!strokeStack.empty()) {
                drawingStrokes.add(strokeStack.pop());
                if (sketchActivity != null) {
                    sketchActivity.setUndoButton(true);
                    sketchActivity.setAddButton(true);
                    sketchActivity.setRedoButton(!strokeStack.empty());
                }
                invalidate();
            }
        } else {
            if (sketchActivity != null)
                sketchActivity.modify();
            unReDo(redoStack, undoStack);
        }
    }

    public void setSketchActivity(SketchActivity sketchActivity) {
        this.sketchActivity = sketchActivity;
    }

    public void addLetter(int id) {
        // Save original state at undo stack
        templateLetterData = new LetterChange(letters.size());
        undoStack.push(templateLetterData);
        redoStack.clear();
        if (sketchActivity != null) {
            sketchActivity.setUndoButton(true);
            sketchActivity.setRedoButton(false);
            sketchActivity.modify();
        }

        Letter letter = new Letter(id);
        letter.setPoint(new Point(-xShift + screenWidth / 2 - letter.getWidth() / 2, -yShift + screenHeight / 2 - letter.getHeight() / 2));
        letters.add(letter);
        selectedLetter = letters.size() - 1;
        invalidate();

    }

    public byte[] getData() {
        if (letters.size() == 0)
            return new byte[]{};
        String data = "";
        for (Letter letter : letters)
            data += 0 + "\t" + letter.getId() + "\t" + letter.getPoint().x + "\t" + letter.getPoint().y +
                    "\t" + letter.getSize() + "\t" + letter.getDegree() + "\t" + letter.getColor() + "\n";
        return data.getBytes();
    }

    public void clear() {
        letters.clear();
        xShift = 0;
        yShift = 0;
        scale = 1;
        selectedLetter = -1;

        undoStack.clear();
        redoStack.clear();
        sketchActivity.setRedoButton(false);
        sketchActivity.setUndoButton(false);
    }

    public void setDataByByteArray(byte[] data) {
        clear();

        if (data.length == 0)
            return;

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
                    int color = Integer.parseInt(split[6]);
                    Letter letter = new Letter(id, color);
                    letter.setPoint(new Point(x, y));
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

        templateLetterData = new LetterChange(selectedLetter, letter.getPoint(), letter.getSize(),
                letter.getDegree(), letter.getColor());
        undoStack.push(templateLetterData);
        redoStack.clear();
        if (sketchActivity != null) {
            sketchActivity.setUndoButton(true);
            sketchActivity.setRedoButton(false);
            sketchActivity.modify();
        }

        letter.setColor(color);
        letter.setSize(size);
        letter.setDegree(degree);
        letter.loadBitmap();

        invalidate();
    }

    public void regulateFitting() {
        if (letters.size() > 0) {
            float xMin = MAX_X, xMax = MIN_X, yMin = MAX_Y, yMax = MIN_Y;
            for (Letter letter : letters) {
                float r = (float) Math.sqrt(letter.getHeight() * letter.getHeight() + letter.getWidth() * letter.getWidth()) / 2 * letter.getSize();
                float w_ = letter.getWidth() / 2;
                float h_ = letter.getHeight() / 2;
                Point point = letter.getPoint();
                float xMin_ = point.x + w_ - r;
                float xMax_ = point.x + w_ + r;
                float yMin_ = point.y + h_ - r;
                float yMax_ = point.y + h_ + r;
                if (xMin_ < xMin)
                    xMin = xMin_;
                if (yMin_ < yMin)
                    yMin = yMin_;
                if (xMax_ > xMax)
                    xMax = xMax_;
                if (yMax_ > yMax)
                    yMax = yMax_;
            }
            float w = xMax - xMin + 50;
            float h = yMax - yMin + 50;
            scale = Math.min(screenWidth / w, screenHeight / h);
            xShift = xPivot * (1 - 1 / scale) - (xMin + 25);
            yShift = yPivot * (1 - 1 / scale) - (yMin + 25);
            regulate();
            invalidate();
        }
    }

    private void regulate() {
        if (xPivot * (1 - 1 / scale) - xShift < MIN_X)
            xShift = xPivot * (1 - 1 / scale) - MIN_X;
        else if (xPivot * (1 + 1 / scale) - xShift > MAX_X)
            xShift = xPivot * (1 + 1 / scale) - MAX_X;
        if (yPivot * (1 - 1 / scale) - yShift < MIN_Y)
            yShift = yPivot * (1 - 1 / scale) - MIN_Y;
        else if (yPivot * (1 + 1 / scale) - yShift > MAX_Y)
            yShift = yPivot * (1 + 1 / scale) - MAX_Y;
        if (scale > MAX_SCALE)
            scale = MAX_SCALE;
        else if (scale < MIN_SCALE)
            scale = MIN_SCALE;
    }

    public void setDrawingMode(boolean drawingMode) {
        this.drawingMode = drawingMode;
        if (drawingMode) {
            drawingPoints = new ArrayList<>();
            drawingStrokes = new ArrayList<>();
            selectedLetter = -1;
            invalidate();
            if (sketchActivity != null) {
                sketchActivity.setUndoButton(false);
                sketchActivity.setRedoButton(false);
            }
        } else {
            if (sketchActivity != null) {
                sketchActivity.setUndoButton(!undoStack.empty());
                sketchActivity.setRedoButton(!redoStack.empty());
            }
        }
    }

    public Bitmap getBitmapImage(boolean drawingMode) {
        if (drawingMode) {
            drawGrid = false;
            drawLetter = false;
            invalidate();

            setDrawingCacheEnabled(true);
            Bitmap bitmap = getDrawingCache();
            Bitmap screenShot = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            setDrawingCacheEnabled(false);

            drawGrid = true;
            drawLetter = true;
            invalidate();

            return screenShot;
        } else {
            float x_ = xShift, y_ = yShift, s_ = scale;
            int selected_ = selectedLetter;

            drawGrid = false;
            selectedLetter = -1;
            regulateFitting();
            setDrawingCacheEnabled(true);
            Bitmap bitmap = getDrawingCache();
            Bitmap screenShot = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            setDrawingCacheEnabled(false);

            drawGrid = true;
            xShift = x_;
            yShift = y_;
            scale = s_;
            selectedLetter = selected_;
            invalidate();

            return screenShot;
        }
    }

    public void setDegree(float degree) {
        this.degree = degree;
    }

    public void setThickness(float thickness) {
        this.thickness = thickness;
    }

    public void setIsCircle(boolean isCircle) {
        this.isCircle = isCircle;
    }

    public float[] getSettings() {
        return new float[]{xShift, yShift, scale};
    }

    public void setSettings(float[] data) {
        xShift = data[0];
        yShift = data[1];
        scale = data[2];
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

        int change, index, color, id;
        Point point;
        float size, degree;

        LetterChange(int index, Point point, float size, float degree, int color) {
            change = CHANGE_LETTER;
            this.index = index;
            this.point = new Point(point.x, point.y);
            this.size = size;
            this.degree = degree;
            this.color = color;
        }

        LetterChange(int index) {
            change = CHANGE_LETTER_ADDITION;
            this.index = index;
        }

        LetterChange(int index, int id, Point point, float size, float degree, int color) {
            change = CHANGE_LETTER_REMOVE;
            this.index = index;
            this.id = id;
            this.point = new Point(point.x, point.y);
            this.size = size;
            this.degree = degree;
            this.color = color;
        }
    }

    private class Stroke {
        private ArrayList<Point> points;
        private boolean circle;
        private float thickness_, degree_;
        private Path path;

        Stroke(boolean circle) {
            this.circle = circle;
            this.points = new ArrayList<>();
            this.points.addAll(drawingPoints);
            this.thickness_ = thickness / scale;
            this.degree_ = degree;

            if (!circle) {
                path = new Path();
                path.moveTo(points.get(0).x + (float) Math.sin(degree_) * thickness_,
                        points.get(0).y - (float) Math.cos(degree_) * thickness_);
                for (int i = 1; i < points.size(); i++) {
                    path.rLineTo(points.get(i).x - points.get(i - 1).x, points.get(i).y - points.get(i - 1).y);
                }
                path.rLineTo(-2 * (float) Math.sin(degree_) * thickness_, 2 * (float) Math.cos(degree_) * thickness_);
                for (int i = points.size() - 1; i > 0; i--) {
                    path.rLineTo(points.get(i - 1).x - points.get(i).x, points.get(i - 1).y - points.get(i).y);
                }
                path.rLineTo(2 * (float) Math.sin(degree_) * thickness_, -2 * (float) Math.cos(degree_) * thickness_);
            }
        }

        void draw(Canvas canvas) {
            if (circle) {
                for (int i = 0; i < points.size() - 1; i++) {
                    Point p1 = points.get(i);
                    Point p2 = points.get(i + 1);
                    paint_drawing.setStrokeWidth(thickness_ * 2);
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint_drawing);
                }
                for (int i = 0; i < points.size(); i++)
                    canvas.drawCircle(points.get(i).x, points.get(i).y, thickness_, paint_drawing);
            } else {
                canvas.drawPath(path, paint_drawing);
            }
        }
    }
}
