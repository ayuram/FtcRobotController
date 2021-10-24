package org.firstinspires.ftc.teamcode.modules.capstone;

import com.noahbres.jotai.StateMachine;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.modules.Module;

/**
 * Module to collect the team marker at the start of the match
 * @author Sreyash Das Sarma
 */
public class Slides extends Module<Slides.State> {
    enum State {
        TRANSIT_IN (0,0.5),
        IN(0.2,0.5),
        TRANSIT_OUT(0.5, 0.5),
        OUT(0.5,0.1);
        final double dist;
        final double time;
        State(double dist,double time) {
            this.dist = dist;
            this.time = time;
        }
    }
    StateMachine<State> stateMachine;
    Servo slideLeft, slideRight;
    Claw claw;
    /**
     * Constructor which calls the 'init' function
     *
     * @param hardwareMap instance of the hardware map provided by the OpMode
     */
    public Slides(HardwareMap hardwareMap) {
        super(hardwareMap);
    }

    /**
     * This function initializes all necessary hardware modules
     */
    @Override
    public void init() {
        slideLeft = hardwareMap.servo.get("slideLeft");
        slideRight = hardwareMap.servo.get("slideRight");
        claw=new Claw(hardwareMap);
        setState(State.IN);
    }

    /**
     * This function updates all necessary controls in a loop
     */
    @Override
    public void update() {
        claw.update();
        switch (getState()) {
            case TRANSIT_IN:
                if (elapsedTime.seconds() > getState().time) {
                    setState(Slides.State.IN);
                }
            case IN:
                in();
                claw.open();
                break;
            case TRANSIT_OUT:
                claw.close();
                if (elapsedTime.seconds() > getState().time) {
                    setState(Slides.State.OUT);
                }
                out();
                break;
            case OUT:
                out();
                claw.open();
                if (elapsedTime.seconds() > getState().time) {
                    setState(Slides.State.TRANSIT_IN);
                }
                break;
        }
    }

    /**
     * @return Whether the module is currently in a potentially hazardous state for autonomous to resume
     */
    private void out() {
        slideLeft.setPosition(0.0);
        slideRight.setPosition(0.26);
    }

    /**
     * Return platform to rest
     */
    private void in() {
        slideLeft.setPosition(0.96);
        slideRight.setPosition(0.7);
    }
    @Override
    public boolean isDoingWork() {
        if(slideLeft.getPosition()!= getState().dist&&elapsedTime.time()>getState().time){
            return true;
        } else return slideLeft.getPosition() != slideRight.getPosition();
    }

    /**
     * @return Whether the module is currently in a hazardous state
     */
    @Override
    public boolean isHazardous() {
        return getState() == Slides.State.OUT || getState() == Slides.State.TRANSIT_OUT;
    }

}
