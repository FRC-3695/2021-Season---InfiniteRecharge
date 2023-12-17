// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.auto;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.commands.ConstantCommandDriveIntake;
import frc.robot.commands.CyborgCommandAlignTurret;
import frc.robot.commands.CyborgCommandEmulatePath;
import frc.robot.commands.CyborgCommandSetTurretPosition;
import frc.robot.commands.CyborgCommandShootPayload;
import frc.robot.commands.CyborgCommandSmartDriveDistance;
import frc.robot.commands.CyborgCommandWait;
import frc.robot.subsystems.SubsystemDrive;
import frc.robot.subsystems.SubsystemFeeder;
import frc.robot.subsystems.SubsystemFlywheel;
import frc.robot.subsystems.SubsystemIntake;
import frc.robot.subsystems.SubsystemReceiver;
import frc.robot.subsystems.SubsystemTurret;

/**
 * A more "traditional" auto used for judging.
 */
public class TraditionalJudgementAuto implements IAuto {
    private InstantCommand zeroDrivetrain;
    private Command doSixBallAuto;
    private ConstantCommandDriveIntake driveIntake;
    private CyborgCommandEmulatePath
        driveTo5PowerCells, // path emulators are listed in the order in which they should be executed.
        driveToSite;
    private CyborgCommandWait
        waitToCollect,
        waitToAlign;
    private CyborgCommandSmartDriveDistance driveToStraightenOut;
    private CyborgCommandSetTurretPosition setTurretPosition;
    private CyborgCommandAlignTurret alignTurret;
    private CyborgCommandShootPayload shootPowerCells;


    public TraditionalJudgementAuto(
        SubsystemDrive drivetrain,
        SubsystemTurret turret,
        SubsystemIntake intake,
        SubsystemFeeder feeder,
        SubsystemFlywheel flywheel,
        SubsystemReceiver kiwilight
    ) {
        this.zeroDrivetrain          = new InstantCommand( () -> { Robot.getRobotContainer().zeroAllDrivetrain(); } );
        this.doSixBallAuto           = new SixBallSimpleAuto(drivetrain, turret, kiwilight, intake, feeder, flywheel).getCommand();
        this.driveIntake             = new ConstantCommandDriveIntake(intake, feeder);
        this.driveTo5PowerCells      = new CyborgCommandEmulatePath(drivetrain, Constants.TRAD_JUDGEMENT_AUTO_DRIVE_TO_POWER_CELLS_FILE);
        this.driveToSite             = new CyborgCommandEmulatePath(drivetrain, Constants.TRAD_JUDGEMENT_AUTO_DRIVE_TO_SITE_FILE);
        this.waitToCollect           = new CyborgCommandWait(750);
        this.waitToAlign             = new CyborgCommandWait(500);
        this.driveToStraightenOut    = new CyborgCommandSmartDriveDistance(drivetrain, 36, 0.15, 0, 1);
        this.setTurretPosition       = new CyborgCommandSetTurretPosition(turret, Constants.TRAD_JUDGEMENT_AUTO_YAW_TARGET, Constants.TRAD_JUDGEMENT_AUTO_PITCH_TARGET);
        this.alignTurret             = new CyborgCommandAlignTurret(turret, kiwilight);
        this.shootPowerCells         = new CyborgCommandShootPayload(intake, feeder, flywheel, turret, Constants.TRAD_JUDGEMENT_AUTO_BALLS_TO_SHOOT, false);
    }

    /**
     * Returns the command that is scheduled to run the auto.
     * Order of events:
     * - Zero drivetrain
     * - Run 6-ball simple auto (zeros turret, drives back and collects, drives forward again, and shoots)
     * - Run Ball intake
     *   - Drive and collect 2 power cells on rendezvous point (TRAD_JUDGEMENT_AUTO_DRIVE_TO_POWER_CELLS_PT_1_FILE)
     *   - Drive forward 36 inches to avoid hitting post
     *   - Drive and collect 3 remaining cells on rendezvous point (TRAD_JUDGEMENT_AUTO_DRIVE_TO_POWER_CELLS_PT_2_FILE)
     * - Drive to the shooting site (TRAD_JUDGEMENT_AUTO_DRIVE_TO_SITE_FILE)
     * - Position turret
     * - Align Turret
     *   - Shoot remaining 5 power cells
     */
    public Command getCommand() {
        //drive to power cells while running intake to collect them
        Command collectPowerCells = driveTo5PowerCells.andThen(waitToCollect);
        Command collectPowerCellsWithIntake = collectPowerCells.raceWith(driveIntake);

        //align turret while shooting 5 power cells
        Command waitThenShoot = waitToAlign.andThen(shootPowerCells);
        Command alignAndShoot = alignTurret.raceWith(waitThenShoot);

        Command finalCommand = zeroDrivetrain.andThen(doSixBallAuto, collectPowerCellsWithIntake, driveToSite, driveToStraightenOut, setTurretPosition, alignAndShoot);
        return finalCommand;
    }

    /**
     * This method will return true in this case because the auto requires the flywheel to be running.
     */
    public boolean requiresFlywheel() {
        return true;
    }
    
}
