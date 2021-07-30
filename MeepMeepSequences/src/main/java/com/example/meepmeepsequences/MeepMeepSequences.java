package com.example.meepmeepsequences;

import com.acmerobotics.roadrunner.geometry.*;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.noahbres.meepmeep.MeepMeep;
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark;

public class MeepMeepSequences {
    public static void main(String[] args) {
        // Declare a MeepMeep instance
        // With a field size of 800 pixels
        MeepMeep mm = new MeepMeep(600)
                // Set field image
                .setBackground(MeepMeep.Background.FIELD_ULTIMATE_GOAL_DARK)
                // Set theme
                .setTheme(new ColorSchemeRedDark())
                // Background opacity from 0-1
                .setBackgroundAlpha(1f)
                // Set constraints: maxVel, maxAccel, maxAngVel, maxAngAccel, track width
                .setConstraints(54, 54, Math.toRadians(180), Math.toRadians(180), 15)
                .followTrajectorySequence(drive ->
                        drive.trajectorySequenceBuilder(new Pose2d(-61.5975, -16.8475, 0))
                                .splineTo(new Vector2d(30.5275, -22.7), Math.toRadians(0))
                                .splineTo(new Vector2d(45.5275, -22.7), Math.toRadians(0))
                                .splineTo(new Vector2d(48.5275, -22.7), Math.toRadians(0))
                                .splineTo(new Vector2d(50.5275, -17.7), Math.toRadians(90))
                                .splineTo(new Vector2d(50.5, 17), Math.toRadians(90))
                                .build()
                )
                .start();

    }
}