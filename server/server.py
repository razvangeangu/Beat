from flask import Flask
import pusher
import soundcloud
import time

pusher_client = pusher.Pusher(
	app_id='187204',
	key='bea8f3b8f2a17f16fefe',
	secret='d3364a1a32afb8627367',
	ssl=True
	)

soundcloud_client = soundcloud.Client(
	client_id='35aaab4bcb9a2d8a4707f3b613f47ab3',
	client_secret='c64f0712668aba23e988a3aea5a2890c',
	)

app = Flask(__name__)

def parse_rooms(name="rooms.txt"):
	f = open(name, 'r')
	rooms = {}
	try:
		for line in f:
			line = line.split(',')
			rooms[line[0]] = line[1].replace('\n','')
	except:
		return {}
	f.close()
	return rooms


def song_fetch(id):
	track = soundcloud_client.get('/tracks/'+str(id))
	stream_url = soundcloud_client.get(track.stream_url, allow_redirects=False)
	return stream_url.location

def push_song(channel, id):
	pusher_client.trigger(channel, 'song_url', {'message':song_fetch(id)})

@app.route('/')
def index():
    return 'Hi'

@app.route('/hello')
def hello_world():
    return 'Hello World!'

@app.route('/test', methods=['GET', 'POST'])
def test():
    pusher_client.trigger('test_channel','my_event',{'message':'hello world'})
    return "WORKS"

@app.route('/host/<name>')
def create_room(name):
	if name in parse_rooms().keys():
		return "False"
	else:
		channel = "channel" + name
		f = open("rooms.txt", 'a+')
		f.write(name+','+channel+'\n')
		f.close()
		return "True,"+channel

@app.route('/join/<name>')
def join_room(name):
	if name in parse_rooms().keys():
		rooms = parse_rooms()
		channel = rooms[name]
		return "True,"+channel
	else:
		return "False"

@app.route('/<params>')
def set_song(params):
	params = params.split(':')
	name = params[0]
	id = params[1]
	if name in parse_rooms().keys():
		rooms = parse_rooms()
		push_song(rooms[name], id)
		return "True"
	else:
		return "False"

@app.route('/start/<name>')
def play(name):
	if name in parse_rooms().keys():
		rooms = parse_rooms()
		pusher_client.trigger(rooms[name], 'play_song', {'message':'play'})
		return "True"
	else:
		return "False"

@app.route('/pause/<name>')
def pause(name):
	rooms = parse_rooms()
	pusher_client.trigger(rooms[name], 'play_song', {'message':'pause'})
	return "True"

@app.route('/stop/<name>')
def stop(name):
	rooms = parse_rooms()
	pusher_client.trigger(rooms[name], 'play_song', {'message':'stop'})
	return "True"


@app.route('/getsongs')
def parse_songs():
	f = open("songs.txt", 'r')
	songs = ""
	for line in f:
		songs += line
	return songs


@app.route('/skip/<params>')
def skip_song(params):
	params = params.split(':')
	name = params[0]
	skip_time = params[1]
	rooms = parse_rooms()
	pusher_client.trigger(rooms[name], 'play_song', {'skip':skip_time})


def deleteContent(fName):
    with open(fName, "w"):
        pass

@app.route('/reset')
def reset():
	deleteContent('rooms.txt')
	return "cleared."
