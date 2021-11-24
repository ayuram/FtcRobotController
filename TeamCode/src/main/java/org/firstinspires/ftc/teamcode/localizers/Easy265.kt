package org.firstinspires.ftc.teamcode.localizers

import com.arcrobotics.ftclib.geometry.Rotation2d
import com.arcrobotics.ftclib.geometry.Transform2d
import com.arcrobotics.ftclib.geometry.Translation2d
import com.intel.realsense.librealsense.UsbUtilities
import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.util.RobotLog
import com.spartronics4915.lib.T265Camera
import org.firstinspires.ftc.teamcode.drive.DriveConstants
import org.firstinspires.ftc.teamcode.util.*
import org.firstinspires.ftc.teamcode.util.field.Details
import kotlin.math.cos

/**
 * Static accessibility class which allows for easy
 * creation and access of the T265 camera, wrapping
 * the FTC265 library to allow more straightforward
 * usage.
 */
@Suppress("UNUSED")
object Easy265 {

    private const val TAG = "Easy265"
    private val ODOMETRY_COVARIANCE = 1.0
    private val INCH_TO_METER = 0.0254
    private val defaultTransform2d = Transform2d(
        Translation2d(-8 * INCH_TO_METER, 1.18 * INCH_TO_METER), Rotation2d(
            Math.toRadians(180.0)
        )
    )
    private lateinit var leftEncoder: Encoder
    private lateinit var rightEncoder: Encoder
    private lateinit var imu: BNO055IMU


    /**
     * The global T265 camera, throws UninitializedPropertyAccessException
     * if the camera instance hasn't been created, call init() to do so
     */
    @JvmStatic lateinit var camera: T265Camera
        private set

    @JvmStatic val isStarted get() = ::camera.isInitialized && camera.isStarted

    /**
     * The camera data returned from the last update.
     * Returns null if update() hasn't been called or the camera hasn't been started
     */
    @JvmStatic var lastCameraUpdate: T265Camera.CameraUpdate? = null

    /**
     * The pose reported from the last camera update, in inches.
     * Returns null if update() hasn't been called or the camera hasn't been started
     * The pose is only sent to the camera if it isn't null and the camera has been started.
     */
    @JvmStatic var lastPose
        //set and get are in inches
        set(value) {
            while (lastCameraUpdate?.confidence == T265Camera.PoseConfidence.Failed) {
                update()
            }
            if(value != null && isStarted) {
                camera.setPose(value.toFTCLibPose2d().toMeters())
            }
        }
        get() = lastCameraUpdate?.pose?.toRRPose2d()?.toInches()

    /**
     * The velocity reported from the last camera update.
     * Returns null if update() hasn't been called or the camera hasn't been started
     */
    @JvmStatic val lastVelocity get() = lastCameraUpdate?.velocity?.toRRPoseVelo()?.toInches()

    /**
     * Initializes and starts the camera. If it has already been started,
     * it updates the data. Recursively tries to open the camera the specified
     * amount of attempts.
     *
     * @param hardwareMap current HardwareMap (tp get the appContext)
     * @param cameraToRobot offset of the center of the robot from the center of the camera
     * @param attempts amount of times this method will be called recursively if the initialization fails
     */
    @JvmStatic @JvmOverloads fun init(
        opMode: OpMode,
        cameraToRobot: Transform2d = defaultTransform2d,
        attempts: Int = 6,
        leftMotor: DcMotorEx,
        rightMotor: DcMotorEx,
        imu: BNO055IMU
    ) {
        leftEncoder = Encoder(leftMotor)
        rightEncoder = Encoder(rightMotor)
        try {
            if(!::camera.isInitialized) {
                UsbUtilities.grantUsbPermissionIfNeeded(opMode.hardwareMap.appContext)
                camera = T265Camera(cameraToRobot, ODOMETRY_COVARIANCE, opMode.hardwareMap.appContext
                )
                UsbUtilities.grantUsbPermissionIfNeeded(opMode.hardwareMap.appContext)
            }

            if(camera.isStarted) {
                update()
                return
            }

            camera.start()
            update()
        } catch(e: Exception) {
            if(attempts <= 0) {
                throw RuntimeException("Unable to start T265Camera after various attempts", e)
            } else {
                RobotLog.w(TAG, "Unable to start T265Camera, retrying...", e)
                init(opMode, cameraToRobot, attempts - 1, leftMotor, rightMotor, imu)
            }
        }
    }

    /**
     * Initializes and start the camera. If it has already been started,
     * it simply updates the data. Infinitely tries to recursively open the
     * camera. Not recommended.
     *
     * @param hardwareMap current HardwareMap (tp get the appContext)
     * @param cameraToRobot offset of the center of the robot from the center of the camera
     */
    @JvmStatic @JvmOverloads fun initWithoutStop(
        opMode: OpMode,
        cameraToRobot: Transform2d = defaultTransform2d,
        leftMotor: DcMotorEx,
        rightMotor: DcMotorEx,
        imu: BNO055IMU,
    ) = init(opMode, cameraToRobot, Int.MAX_VALUE, leftMotor, rightMotor, imu)

    /**
     * Pulls the last data from the camera and stores it the
     * lastCameraUpdate variable, therefore updating the data
     * returned by the lastPose or lastVelocity properties.
     *
     * If the camera hasn't been started (or instantiated) no update happens.
     */
    @JvmStatic fun update() {
        if(isStarted) {
            if (Thread.interrupted()) {
                camera.stop()
                return
            }
            lastCameraUpdate = camera.lastReceivedCameraUpdate
            val pitch = imu.angularOrientation.thirdAngle
            Details.telemetry.addData("Pitch", Math.toDegrees(pitch.toDouble()))
            Details.telemetry.update()
            camera.sendOdometry(DriveConstants.rpmToVelocity((leftEncoder.correctedVelocity + rightEncoder.correctedVelocity) / 2) * INCH_TO_METER * cos(pitch.toDouble()), 0.0)
        }
    }

    @JvmStatic fun stop() {
        camera.stop()
    }

}