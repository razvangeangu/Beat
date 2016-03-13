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

class HostViewController : UIViewController, UITableViewDelegate, UITableViewDataSource {
    
    var player = AVPlayer()
    var channelName : String! = "None"
    var channelNo : String! = "None"
    var songURLString : String! = "None"
    var songID : String! = ""
    var songs = [Media]()
    var shouldChangeButton = true
    
    @IBOutlet var slider: UISlider!
    @IBOutlet weak var currentTime: UILabel!
    @IBOutlet weak var songLabel: UILabel!
    @IBOutlet weak var tableView: UITableView!
    @IBOutlet weak var playButton: UIButton!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
        
        getSongsFromServer()
        getChannel()
        tableView.delegate = self
        tableView.dataSource = self
        
        var Timer = NSTimer.scheduledTimerWithTimeInterval(1.0, target: self, selector: Selector("updateSlider"), userInfo: nil, repeats: true)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func tableView(tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return self.songs.count
    }
    
    func tableView(tableView: UITableView, cellForRowAtIndexPath indexPath: NSIndexPath) -> UITableViewCell {
        let cell = self.tableView.dequeueReusableCellWithIdentifier("cell", forIndexPath: indexPath) as UITableViewCell
        
        var media : Media
        
        media = songs[indexPath.row]
        
        cell.textLabel?.text = media.name
        
        return cell
    }
    
    func tableView(tableView: UITableView, didSelectRowAtIndexPath indexPath: NSIndexPath) {
        print(songs[indexPath.row].getID())
        songID = songs[indexPath.row].getID()
        songLabel.text = songs[indexPath.row].getName()
    }
    
    func getSongsFromServer() {
        let url = NSURL(string: "https://fb-beat.herokuapp.com/getsongs")
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
                let dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)! as String
                let mediaDetails = dataString.componentsSeparatedByString(";")
                
                for i in 0 ..< 10 {
                    self.songs.append(Media(name: mediaDetails[i].componentsSeparatedByString(",")[1],id: mediaDetails[i].componentsSeparatedByString(",")[0]))
                    self.tableView.reloadData()
                }
            })
        }
        
        task.resume()
    }
    
    @IBAction func sliderChanged(sender: AnyObject) {
        print(slider.value)
        player.pause()
        player.seekToTime(CMTime(seconds: Double(slider.value), preferredTimescale: self.player.currentItem!.asset.duration.timescale))
        player.play()
    }
    
    @IBAction func playButtonAction(sender: AnyObject) {
        
        if playButton.currentImage == UIImage(named: "playButton") && shouldChangeButton {
            let urlS = NSURL(string: "https://fb-beat.herokuapp.com/\(channelName):\(songID)")
            print(urlS)
            let taskS = NSURLSession.sharedSession().dataTaskWithURL(urlS!) { (data, response, error) in
                dispatch_async(dispatch_get_main_queue(), {
//                    let resultString = NSString(data: data!, encoding: NSUTF8StringEncoding)! as String
//                    print(resultString)
                })
            }
            
            taskS.resume()
            
            let url = NSURL(string: "https://fb-beat.herokuapp.com/start/\(channelName)")
            let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
                dispatch_async(dispatch_get_main_queue(), {
                
                })
            }
            
            task.resume()
            
            playButton.setImage(UIImage(named: "pauseButton"), forState: UIControlState.Normal)
            shouldChangeButton = false
        } else {
            let url = NSURL(string: "https://fb-beat.herokuapp.com/pause/\(channelName)")
            let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
                dispatch_async(dispatch_get_main_queue(), {

                })
            }
            
            task.resume()
            
            playButton.setImage(UIImage(named: "playButton"), forState: UIControlState.Normal)
            shouldChangeButton = true
        }
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
        
        slider.maximumValue = Float((player.currentItem?.asset.duration.seconds)!)
    }
    
    func updateSlider() {
        slider.value += 1
        if (slider.value < 10) {
            currentTime.text = "00:0\(slider.value)"
        }
        
        if slider.value >= 10 && slider.value < 59 {
            currentTime.text = "00:\(slider.value)"
        }
        
        if slider.value > 59 {
            currentTime.text = "01:\(slider.value - 60)"
        }
        
        if slider.value > 119 {
            currentTime.text = "01:\(slider.value - 120)"
        }
//        if !shouldChangeButton {
//            currentTime.text = String(Float((player.currentItem?.currentTime().seconds)!))
//            slider.value = Float((player.currentItem?.currentTime().seconds)!)
//        }
    }
    
    func getChannel() {
        let url = NSURL(string: "https://fb-beat.herokuapp.com/host/\(channelName)")
        let task = NSURLSession.sharedSession().dataTaskWithURL(url!) { (data, response, error) in
            dispatch_async(dispatch_get_main_queue(), {
                let dataString = NSString(data: data!, encoding: NSUTF8StringEncoding)!
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
                
                if message == "pause" {
                    self.player.pause()
                    self.player.rate = 0.0
                }
            }
        })
    }
    
}