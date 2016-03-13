//
//  HostViewController.swift
//  Beat
//
//  Created by Razvan-Gabriel Geangu on 3/12/16.
//  Copyright © 2016 Razvan-Gabriel Geangu. All rights reserved.
//

import UIKit
import AVFoundation
import PusherSwift

class HostViewController : UIViewController {
    
    var player = AVPlayer()
    var channelName : String! = "None"
    var channelNo : String! = "None"
    var songName : String! = "None"
    var songURLString : String! = "None"
    
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var playButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        getSongsFromServer()
        getChannel()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func getSongsFromServer() {
        let songs = [
            ("first"),
            ("seconds")
        ]
        let url = NSURL(string: "https://fb-beat.herokuapp.com/getsongs")
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
                var dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)!
                print("Datastring: " + (dataString as String))
                
//                if dataString == "True" {
//                    var alert = UIAlertController(title: "Congratulations", message: "Your song has been found", preferredStyle: UIAlertControllerStyle.Alert)
//                    alert.addAction(UIAlertAction(title: "OK", style: UIAlertActionStyle.Default, handler: nil))
//                    self.presentViewController(alert, animated: true, completion: nil)
//                }
//                for var i = 0; i < 5; i++ {
//                    songs.append((i as? String)!)
//                }
                
            })
        }
        
        task.resume()
    }
    
    
    
    @IBAction func playButtonAction(sender: AnyObject) {
        let url = NSURL(string: "https://fb-beat.herokuapp.com/start/\(channelName)")
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
//                var dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)!
//                print("hello " + (dataString as String))
            })
        }
        
        task.resume()
        
        playButton.setBackgroundImage(UIImage(named: "pauseButton"), forState: UIControlState.Normal)
    }
    
    @IBAction func stopButtonAction(sender: AnyObject) {
        player.pause()
        player.rate = 0.0
    }
    
    func playMedia(mediaURL : String) {
        let playerItem = AVPlayerItem( URL:NSURL( string:mediaURL )! )
        player = AVPlayer(playerItem:playerItem)
        player.rate = 1.0
        player.play()
    }
    
    func getChannel() {
        let url = NSURL(string: "https://fb-beat.herokuapp.com/host/\(channelName)")
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
                var dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)!
                self.channelNo = dataString.substringWithRange(NSRange(location: 5, length: dataString.length - 5))
                NSLog("Assigning channel: " + dataString.substringWithRange(NSRange(location: 5, length: dataString.length - 5)))
                self.pusherMessage()
            })
        }
        
        task.resume()
    }
    
    func pusherMessage() {
        let pusher = Pusher(key: "bea8f3b8f2a17f16fefe")
        pusher.connect()
        
        let myChannel = pusher.subscribe(channelNo)
        NSLog("Subscribed to channel: " + channelNo)
        
        myChannel.bind("song_url", callback: { (data: AnyObject?) -> Void in
            
            if let data = data as? Dictionary<String, AnyObject> {
                
                let message  = data["message"] as! String
                
                NSLog("Pusher message: " + message)
                
                print(message)
                
                self.songURLString = message
            }
        })
        
        myChannel.bind("play_song", callback: { (data: AnyObject?) -> Void in
            
            if let data = data as? Dictionary<String, AnyObject> {
                
                let message  = data["message"] as! String
                
                NSLog("Pusher message: " + message)
                
                if message == "play" {
                    print(self.songURLString)
                    self.playMedia(self.songURLString)
                }
                
//                if message == "False" {
//                    self.player.pause()
//                    self.player.rate = 0.0
//                }
            }
        })
    }
    
}