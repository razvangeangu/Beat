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
    
    @IBOutlet weak var textField: UITextField!
    @IBOutlet weak var playButton: UIButton!
    @IBOutlet weak var stopButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        getChannel()
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    @IBAction func searchButtonAction(sender: AnyObject) {
        songName = textField.text
//        NSLog("Searching for song: " + songName)
        let url = NSURL(string: "https://fb-beat.herokuapp.com/\(channelName):\(songName)")
//        print(url)
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
                var dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)!
                print("Datastring: " + (dataString as String))
            })
        }
        
        task.resume()
    }
    
    @IBAction func playButtonAction(sender: AnyObject) {
        let url = NSURL(string: "https://fb-beat.herokuapp.com/start/\(channelName)")
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
                var dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)!
                print(dataString)
            })
        }
        
        task.resume()
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
//        NSLog("Subscribed to channel: " + channelNo)
        
        myChannel.bind("song_url", callback: { (data: AnyObject?) -> Void in
            
            if let data = data as? Dictionary<String, AnyObject> {
                
                let message  = data["message"] as! String
                
                NSLog("Pusher message: " + message)
                
                self.songURLString = message
            }
        })
        
        myChannel.bind("play_song", callback: { (data: AnyObject?) -> Void in
            
            if let data = data as? Dictionary<String, AnyObject> {
                
                let message  = data["message"] as! String
                
//                NSLog("Pusher message: " + message)
                
                if message == "play" {
                    self.playMedia(self.songURLString)
                }
                
                if message == "stop" {
                    self.player.pause()
                    self.player.rate = 0.0
                }
            }
        })
    }
    
}