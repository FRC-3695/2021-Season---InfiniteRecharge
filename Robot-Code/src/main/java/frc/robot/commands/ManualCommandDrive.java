/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.commands;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.CommandBase;
import frc.robot.Robot;
import frc.robot.subsystems.SubsystemDrive;
import frc.robot.util.Util;

public class ManualCommandDrive extends CommandBase {
  private SubsystemDrive drivetrain;

  /**
   * Creates a new ManualCommandDrive.
   */
  public ManualCommandDrive(SubsystemDrive drivetrain) {
    this.drivetrain = drivetrain;

    addRequirements(drivetrain);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {
  }

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    // check to see if controllers are good before potentially making the robot destroy the lives of the entire team
    if (Robot.getRobotContainer().controllersGood() || Util.getAndSetBoolean("Override Drive Lock", false)) {
      Joystick driver = Robot.getRobotContainer().getDriver();
      Joystick driver2 = Robot.getRobotContainer().getDriver2();

      switch (Robot.getRobotContainer().getDriveScheme()) {
      case RL:
        drivetrain.DriveTankByController(driver);
        break;
      case TRUE_TANK:
        drivetrain.driveTankTrue(driver, driver2);
        break;
      }

      SmartDashboard.putBoolean("Drivetrain Active", true);
    } else { //the controllers are not good, lock the drivetrain
      drivetrain.setLeftPercentOutput(0); 
      drivetrain.setRightPercentOutput(0);
      SmartDashboard.putBoolean("Drivetrain Active", false);
      DriverStation.reportError("DRIVETRAIN LOCKED, CHECK DASHBOARD CONFIG TAB", false);
    }
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return false;
  }
}
