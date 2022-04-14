package org.firstinspires.ftc.teamcode.trajectorysequenceimproved.sequencesegment

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker

class ConditionalWait(startPose: Pose2d, markers: List<TrajectoryMarker>, var runnable: Runnable) : SequenceSegment(
    { 0.0 },
    startPose,
    startPose,
    markers
)