//
//  ViewController.swift
//  Beat
//
//  Created by Razvan-Gabriel Geangu on 3/12/16.
//  Copyright © 2016 Razvan-Gabriel Geangu. All rights reserved.
//

import UIKit
import AVFoundation
import PusherSwift

class ViewController: UIViewController {
    
    var channel : String!
    var connectionType : String!
    var typeOfUser : String!
    var timesPressed : Int = 1
    
    @IBOutlet weak var topButton: UIButton!
    @IBOutlet weak var bottomButton: UIButton!
    @IBOutlet weak var textField: UITextField!
    @IBOutlet weak var joinButton: UIButton!

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func topButtonPressed(sender: AnyObject) {
        if timesPressed == 2 {
            typeOfUser = "host"
            topButton.hidden = true
            bottomButton.hidden = true
            textField.hidden = false
            joinButton.hidden = false
            timesPressed += 1
            joinButton.setTitle("Create", forState: UIControlState.Normal)
        } else {
            connectionType = "network"
            topButton.setTitle("Host", forState: UIControlState.Normal)
            bottomButton.setTitle("Client", forState: UIControlState.Normal)
            bottomButton.enabled = true
            timesPressed += 1
        }
    }
    
    @IBAction func bottomButtonPressed(sender: AnyObject) {
        if timesPressed == 2 {
            typeOfUser = "client"
            topButton.hidden = true
            bottomButton.hidden = true
            textField.hidden = false
            joinButton.hidden = false
            timesPressed += 1
        } else {
            connectionType = "bluetooth"
            topButton.setTitle("Host", forState: UIControlState.Normal)
            bottomButton.setTitle("Client", forState: UIControlState.Normal)
            joinButton.setTitle("Join", forState: UIControlState.Normal)
            bottomButton.enabled = true
            timesPressed += 1
        }
    }
    
    @IBAction func joinButtonPressed(sender: AnyObject) {
        channel = textField.text
        textField.hidden = true
        joinButton.hidden = true
        
        if connectionType == "network" {
            if typeOfUser == "host" {
                self.performSegueWithIdentifier("hostControllerSegue", sender: nil)
            }
            
            if typeOfUser == "client" {
                self.performSegueWithIdentifier("clientControllerSegue", sender: nil)
            }
        }
        
        if connectionType == "bluetooth" {
            if typeOfUser == "host" {
                self.performSegueWithIdentifier("hostControllerSegue", sender: nil)
            }
            
            if typeOfUser == "client" {
                self.performSegueWithIdentifier("clientControllerSegue", sender: nil)
            }
        }
        
    }

    override func prepareForSegue(segue: UIStoryboardSegue, sender: AnyObject?) {
        if segue.identifier == "hostControllerSegue" {
            if let destinationVC = segue.destinationViewController as? HostViewController {
                destinationVC.channelName = channel
            }
        }
        
        if segue.identifier == "clientControllerSegue" {
            if let destinationVC = segue.destinationViewController as? ClientViewController {
                destinationVC.channelName = channel
            }
        }
    }


}

