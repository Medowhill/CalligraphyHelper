package com.hwajung.ksa.calligraphyhelper.view;

import android.content.Context;
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
    // 어플리케이션의 메인 스케치 화면을 표시할 view

    // 이 view를 호출한 activity
    private SketchActivity sketchActivity;

    // Paint
    // Letter paint, 선택된 letter 테두리 paint, x, y축 paint, 격자 paint
    private Paint paint_letter, paint_selected, paint_axis, paint_line;

    // 화면을 터치하고 있는 pointer의 list
    private ArrayList<Pointer> pointers;

    // 현재 선택된 letter
    private int selectedLetter = -1;

    // 화면에 표시될 letter의 list
    private ArrayList<Letter> letters;

    // Undo, redo를 위해 letter의 배치를 저장할 stack
    private Stack<float[][]> redoStack, undoStack;
    private float[][] templateLetterData;

    // Canvas의 x, y방향 평행 이동
    private float xShift, yShift;

    // Canvas의 확대, 축소 배율
    private float scale = 1;

    // 화면의 가로, 세로 길이 및 확대, 축소 시 기준 점 좌표
    private int screenWidth, screenHeight, xPivot, yPivot;

    // 화면에 표시되고 있는 격자의 간격
    private int interval = 160;

    public SketchView(Context context, AttributeSet attrs) {
        // Constructor
        super(context, attrs);

        Log.i("TEST", "KSA");

        // Letter class에 resource 제공
        Letter.setResources(getResources());

        // Paint instance 생성
        paint_letter = new Paint();

        paint_selected = new Paint();
        paint_selected.setColor(getResources().getColor(R.color.sketch_selected));
        paint_selected.setStyle(Paint.Style.STROKE);

        paint_axis = new Paint();
        paint_axis.setColor(getResources().getColor(R.color.sketch_axis));

        paint_line = new Paint();
        paint_line.setColor(getResources().getColor(R.color.sketch_line));

        // Letter, pointer list 초기화
        letters = new ArrayList<>();
        pointers = new ArrayList<>();

        // Redo, undo 용 stack 초기화
        redoStack = new Stack<>();
        undoStack = new Stack<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // 화면의 크기가 변경될 때 화면 가로, 세로 길이 및 확대, 축소 기준점 재설정
        super.onSizeChanged(w, h, oldw, oldh);

        screenWidth = w;
        screenHeight = h;
        xPivot = screenWidth / 2;
        yPivot = screenHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // Canvas 배경을 흰 색으로
        canvas.drawColor(Color.WHITE);

        // Canvas의 축소, 확대 및 x, y 방향 평행 이동 적용
        canvas.scale(scale, scale, xPivot, yPivot);
        canvas.translate(xShift, yShift);

        // x, y축을 그리기 위한 기준점
        Point leftTop = new Point(0, 0).transform(scale, xPivot, yPivot, xShift, yShift);
        Point rightBottom = new Point(screenWidth, screenHeight).transform(scale, xPivot, yPivot, xShift, yShift);

        // x, y축을 그리기 위한 paint의 stroke width를 확대/축소에 맞추어 조정
        paint_axis.setStrokeWidth(getResources().getInteger(R.integer.sketch_axis_stroke) / scale);

        // x, y축 그리기
        canvas.drawLine(0, leftTop.y, 0, rightBottom.y, paint_axis);
        canvas.drawLine(leftTop.x, 0, rightBottom.x, 0, paint_axis);

        // 격자를 그리기 위한 paint의 stroke width를 확대/축소에 맞추어 조정
        paint_line.setStrokeWidth(getResources().getInteger(R.integer.sketch_line_stroke) / scale);

        // 격자 그리기
        for (int i = (int) (leftTop.x / interval) - 1; i < (int) (rightBottom.x / interval) + 1; i++)
            if (i != 0)
                canvas.drawLine(i * interval, leftTop.y, i * interval, rightBottom.y, paint_line);
        for (int i = (int) (leftTop.y / interval) - 1; i < (int) (rightBottom.y / interval) + 1; i++)
            if (i != 0)
                canvas.drawLine(leftTop.x, i * interval, rightBottom.x, i * interval, paint_line);

        for (int i = 0; i < letters.size(); i++) {
            // 모든 letter를 화면에 그린다

            // 그릴 letter instance
            Letter letter = letters.get(i);

            // 그릴 letter의 위치 좌표
            float x = letter.getPoint().x;
            float y = letter.getPoint().y;

            // Letter의 scale과 degree에 맞게 canvas 조절
            canvas.scale(letter.getSize(), letter.getSize(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);
            canvas.rotate(letter.getDegree(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);

            // Letter bitmap 그리기
            canvas.drawBitmap(letter.getBitmap(), x, y, paint_letter);

            // 선택된 letter일 경우 테두리를 그린다
            if (i == selectedLetter) {
                paint_selected.setStrokeWidth(getResources().getInteger(R.integer.sketch_selected_stroke) / letter.getSize() / scale);
                canvas.drawRect(x, y, x + letter.getWidth(), y + letter.getHeight(), paint_selected);
            }

            // Canvas를 원래대로 되돌림
            canvas.rotate(-letter.getDegree(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);
            canvas.scale(1.f / letter.getSize(), 1.f / letter.getSize(), x + letter.getWidth() / 2,
                    y + letter.getHeight() / 2);

        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Activity의 메뉴가 안보이게 한다
        if (sketchActivity != null)
            sketchActivity.closeMenu();

        // Event의 좌표 및 ID
        final float x = event.getX();
        final float y = event.getY();
        final int id = event.getPointerId(event.getActionIndex());

        // 화면 상의 절대 좌표와 canvas 상의 상대적인 좌표
        Point point = new Point(x, y);
        Point trans = point.transform(scale, xPivot, yPivot, xShift, yShift);
        Point prevTrans;

        Pointer pointer, pointer0, pointer1;
        Letter letter;

        switch (event.getActionMasked()) {

            // primary-pointer 누름
            case MotionEvent.ACTION_DOWN:
                // 새로운 pointer instance를 만들어 list에 추가
                pointer = new Pointer(id, new Point(x, y));
                pointers.add(pointer);

                if (selectedLetter != -1) {
                    // 선택된 letter가 존재하는 경우
                    letter = letters.get(selectedLetter);
                    if (touchLetter(trans, letter))
                        // 선택된 letter를 터치한다면 해당 letter를 이동시키는 pointer로 지정
                        pointer.role = Pointer.ROLE_LETTER_MOVE;
                }

                // 기존 letter의 정보를 저장
                templateLetterData = new float[letters.size()][];
                for (int i = 0; i < letters.size(); i++)
                    templateLetterData[i] = letters.get(i).toFloatArray();

                // View가 터치되는 동안은 undo, redo가 불가능하도록 설정
                if (sketchActivity != null) {
                    sketchActivity.setRedoButton(false);
                    sketchActivity.setUndoButton(false);
                }

                break;

            // non-primary-pointer 누름
            case MotionEvent.ACTION_POINTER_DOWN:
                // primary-pointer의 역할 삭제
                pointers.get(0).role = Pointer.ROLE_NONE;

                // 새로운 pointer instance를 만들어 list에 추가
                pointer = new Pointer(id, new Point(x, y));
                pointer.role = Pointer.ROLE_NONE;
                pointers.add(pointer);

                break;

            // primary-pointer 뗌
            case MotionEvent.ACTION_UP:
                // 해당하는 pointer를 list에서 제거
                pointer = findPointer(id);
                pointers.remove(pointer);

                if (pointer.moved <= Pointer.MOVE_LIMIT && pointer.role == Pointer.ROLE_UNCERTAIN) {
                    // Pointer가 움직이지 않았고 역할이 정해지지 않았다면 letter 선택을 위한 pointer인지 확인
                    boolean b = true;
                    for (int i = 0; i < letters.size(); i++) {
                        letter = letters.get(i);
                        if (touchLetter(trans, letter)) {
                            // Pointer가 letter를 터치했다면 해당 letter 선택
                            selectedLetter = i;
                            b = false;
                        }
                    }
                    if (b)
                        // Pointer가 아무 letter도 터치하지 않았다면 선택된 letter가 없도록 한다
                        selectedLetter = -1;
                }

                // View 터치가 종료되면 undo, redo가 가능하도록 설정
                if (sketchActivity != null) {
                    sketchActivity.setRedoButton(!redoStack.empty());
                    sketchActivity.setUndoButton(!undoStack.empty());
                }

                if (!(pointer.role == Pointer.ROLE_CANVAS_MOVE || pointer.role == Pointer.ROLE_UNCERTAIN)) {
                    // Letter 정보에 변화가 있는 경우 stack에 기존 정보를 저장한다
                    undoStack.push(templateLetterData);
                    redoStack.clear();
                    if (sketchActivity != null) {
                        sketchActivity.setRedoButton(false);
                        sketchActivity.setUndoButton(true);
                    }
                }

                break;

            // non-primary-pointer 뗌
            case MotionEvent.ACTION_POINTER_UP:
                // 해당하는 pointer를 list에서 제거
                pointer = findPointer(id);
                pointers.remove(pointer);

                break;

            // pointer 움직임
            case MotionEvent.ACTION_MOVE:

                if (event.getPointerCount() == 1) {
                    // Pointer가 하나인 경우 canvas나 letter의 평행 이동이다

                    // Pointer instance를 찾아 움직임을 기록
                    pointer = findPointer(id);
                    pointer.moved++;

                    // Pointer 역할이 미정이고 충분히 움직였다면 canvas를 움직이는 pointer로 지정
                    if (pointer.role == Pointer.ROLE_UNCERTAIN && pointer.moved > Pointer.MOVE_LIMIT)
                        pointer.role = Pointer.ROLE_CANVAS_MOVE;

                    // Pointer의 전 좌표의 canvas 상 상대적인 좌표
                    prevTrans = pointer.prev.transform(scale, xPivot, yPivot, xShift, yShift);

                    switch (pointer.role) {
                        case Pointer.ROLE_LETTER_MOVE:
                            // letter의 평행 이동을 위한 pointer이면 letter의 위치 좌표 변경
                            letter = letters.get(selectedLetter);
                            letter.setPoint(new Point(letter.getPoint().x + trans.x - prevTrans.x,
                                    letter.getPoint().y + trans.y - prevTrans.y));
                            break;
                        case Pointer.ROLE_CANVAS_MOVE:
                            // canvas의 평행 이동을 위한 pointer이면 canvas의 x, y 방향 평행 이동 변경
                            xShift += trans.x - prevTrans.x;
                            yShift += trans.y - prevTrans.y;
                            break;
                    }

                    pointer.prev = point;
                } else {
                    // Pointer가 2개 이상인 경우 앞 두 개의 pointer를 통해 확대, 축소 및 회전 수행
                    pointer0 = pointers.get(0);
                    pointer1 = pointers.get(1);

                    // 이동 전 좌표들의 거리 및 각도
                    float prevDistance = Point.distance(pointer0.prev, pointer1.prev);
                    float prevDegree = Point.degree(pointer0.prev, pointer1.prev);

                    // 이동 기록
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        pointers.get(i).prev = new Point(event.getX(i), event.getY(i));
                    }

                    // 이동 후 좌표들의 거리 및 각도
                    float distance = Point.distance(pointer0.prev, pointer1.prev);
                    float degree = Point.degree(pointer0.prev, pointer1.prev);

                    // 이동 전 값이 의미 있는 경우에만 수행
                    if (prevDistance != 0 && distance != 0) {
                        if (selectedLetter == -1) {
                            // 선택된 letter가 없으면 canvas의 scale 조정
                            scale *= distance / prevDistance;
                        } else {
                            // 선택된 letter가 있으면 확대, 축소 및 회전
                            letter = letters.get(selectedLetter);
                            letter.setSize(letter.getSize() * distance / prevDistance);
                            letter.setDegree(letter.getDegree() + degree - prevDegree);
                        }
                    }
                }

                break;
        }

        // View 새로 그리기
        invalidate();

        return true;
        // return super.onTouchEvent(event);
    }

    private Pointer findPointer(int id) {
        // ID를 통해서 포인터 리스트에서 포인터를 찾는 메서드

        for (Pointer pointer : pointers)
            if (pointer.id == id)
                return pointer;
        return null;
    }

    private boolean touchLetter(Point point, Letter letter) {
        // 주어진 point가 letter를 터치하는지 확인하는 메서드

        float x = point.x, y = point.y;
        float x1 = x - letter.getPoint().x - letter.getWidth() / 2;
        x1 /= letter.getSize();
        float y1 = y - letter.getPoint().y - letter.getHeight() / 2;
        y1 /= letter.getSize();

        double x2 = x1 * Math.cos(-letter.getDegree() / 180 * Math.PI) - y1 * Math.sin(-letter.getDegree() / 180 * Math.PI);
        double y2 = x1 * Math.sin(-letter.getDegree() / 180 * Math.PI) + y1 * Math.cos(-letter.getDegree() / 180 * Math.PI);

        return -letter.getWidth() / 2 < x2 && x2 < letter.getWidth() / 2 && -letter.getHeight() / 2 < y2 && y2 < letter.getHeight() / 2;
    }

    public void undo() {
        // undo를 실행하는 메서드

        if (!undoStack.empty()) {
            // undo stack이 비어있지 않은 경우에만

            // 기존 상태를 redo stack에 저장
            templateLetterData = new float[letters.size()][];
            for (int i = 0; i < letters.size(); i++)
                templateLetterData[i] = letters.get(i).toFloatArray();
            redoStack.push(templateLetterData);

            // undo stack에 저장된 데이터로 새로 letter list 만듦
            letters.clear();
            float[][] letterData = undoStack.pop();
            for (int i = 0; i < letterData.length; i++)
                letters.add(Letter.getLetter(letterData[i]));

            // undo, redo 버튼 설정
            if (sketchActivity != null) {
                if (undoStack.empty())
                    sketchActivity.setUndoButton(false);
                sketchActivity.setRedoButton(true);
            }

            // 화면 갱신
            invalidate();
        }
    }

    public void redo() {
        // redo를 실행하는 메서드
        if (!redoStack.empty()) {
            // redo stack이 비어있지 않은 경우에만

            // 기존 상태를 undo stack에 저장
            templateLetterData = new float[letters.size()][];
            for (int i = 0; i < letters.size(); i++)
                templateLetterData[i] = letters.get(i).toFloatArray();
            undoStack.push(templateLetterData);

            // redo stack에 저장된 데이터로 새로 letter list 만듦
            letters.clear();
            float[][] letterData = redoStack.pop();
            for (int i = 0; i < letterData.length; i++)
                letters.add(Letter.getLetter(letterData[i]));

            // undo, redo 버튼 설정
            if (sketchActivity != null) {
                if (redoStack.empty())
                    sketchActivity.setRedoButton(false);
                sketchActivity.setUndoButton(true);
            }

            // 화면 갱신
            invalidate();
        }
    }

    public void setSketchActivity(SketchActivity sketchActivity) {
        this.sketchActivity = sketchActivity;
    }

    public void addLetter(int id) {
        Letter letter = Letter.getLetter(id, new Point(-xShift, -yShift));
        if (letter != null) {
            // 기존 상태를 undo stack에 저장
            templateLetterData = new float[letters.size()][];
            for (int i = 0; i < letters.size(); i++)
                templateLetterData[i] = letters.get(i).toFloatArray();
            undoStack.push(templateLetterData);

            letters.add(letter);
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

    // Pointer class
    // 화면을 터치하는 각각의 pointer instance
    private class Pointer {

        // 역할에 대한 상수
        // 역할 미정, canvas의 평행 이동, letter의 평행 이동, 역할 없음
        final static int ROLE_UNCERTAIN = -1, ROLE_CANVAS_MOVE = 0, ROLE_LETTER_MOVE = 1, ROLE_NONE = 2;

        // 이동을 지시하는 pointer가 되기 위한 최소의 이동 횟수
        final static int MOVE_LIMIT = 2;

        // Pointer의 ID
        int id;

        // Pointer가 수행하는 역할, 기본 값은 역할 미정
        int role = ROLE_UNCERTAIN;

        // Pointer가 화면을 터치하면서 이동한 횟수
        int moved = 0;

        // Pointer가 이동하기 전 위치의 좌표
        Point prev;

        // Constructor
        Pointer(int id, Point prev) {
            this.id = id;
            this.prev = prev;
        }
    }
}
