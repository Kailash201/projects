import math
import googleapiclient.discovery
import googleapiclient.errors
import requests
from flask import Flask, render_template, redirect, request
import base64

API_KEY = "AIzaSyA5iqFQnLL-cgYCcGOtZXKrpTyp3McmnmA"  # Get from google console developer
chanId = "UCcP7HBCO1_mcTkbG35zJFcQ"  # Your youtube channel ID

client_id = "8fcfd5bf7f7f4356b81b58828f0d6a74"  # Get your key from Spotify developer
client_secret = "a6a31336266446e79c1e4123f4a653fb"  # Get your key from Spotify developer

# Spotify endpoints
redirect_url = "http://127.0.0.1:5000/redirect"  # redirect_url = your local host/redirect
auth_url = "https://accounts.spotify.com/authorize"
api_token = "https://accounts.spotify.com/api/token"
spot_api = "https://api.spotify.com/v1"

app = Flask(__name__)

listOfVideos = []
name_of_youtube_pl = "Swing"  # The name of your youtube playlist to add to your spotify (case sensitive)
vidInd = 0


def main():
    global items, vidInd

    api_service_name = "youtube"
    api_version = "v3"

    # Get credentials and create an API client
    youtube = googleapiclient.discovery.build(
        api_service_name, api_version, developerKey=API_KEY)
    run = True
    nextPage = ""

    listOfPlaylists = []  # store user's playlist and its id
    while run:
        request = youtube.playlists().list(

            part="snippet",
            channelId=chanId,
            pageToken=nextPage,
            maxResults=50

        )
        response = request.execute()
        items = response['items']
        try:
            for x in range(len(items)):
                listOfPlaylists.append(items[x]['snippet']['title'])
                listOfPlaylists.append(items[x]['id'])

            nextPage = response['nextPageToken']

        except KeyError:
            run = False

    for x in range(0, len(listOfPlaylists), 2):

        innerList = []
        innerList.append(listOfPlaylists[x])  # add title of playlist at the start of each row
        nextPageOne = ""
        runOne = True

        while runOne:
            pltRequest = youtube.playlistItems().list(

                part="snippet, contentDetails",
                playlistId=listOfPlaylists[x + 1],
                maxResults=50,
                pageToken=nextPageOne

            )
            pltResponse = pltRequest.execute()
            items = pltResponse['items']
            try:
                for y in range(len(items)):
                    innerList.append(items[y]['snippet']['title'])  # video title
                    innerList.append(items[y]['contentDetails']['videoId'])  # video ID

                nextPageOne = pltResponse['nextPageToken']

            except KeyError:
                runOne = False

        listOfVideos.append(innerList)

    # get index for required playlist
    for x in range(math.floor(len(listOfPlaylists) / 2)):
        if name_of_youtube_pl in listOfVideos[x][0]:
            vidInd = x


# page to request auth via spotify

@app.route("/")
def login():
    # get permissions to edit playlists
    scope = 'playlist-modify-private playlist-modify-public user-read-private'

    # create url to redirect user to spotify, then redirect it back to redirect url
    tmp = requests.get(auth_url, {'client_id': client_id,
                                  'response_type': 'code',
                                  'redirect_uri': redirect_url,
                                  'scope': scope,
                                  'show_dialog': 'true'
                                  })

    return redirect(tmp.url)


# get auth key and exchange it for access token
@app.route("/redirect")
def auth():
    args = request.args
    aut_code = args.get('code')
    tmpString = client_id + ':' + client_secret
    stringbytes = tmpString.encode("ascii")
    base64_bytes = base64.b64encode(stringbytes)
    base64_string = base64_bytes.decode("ascii")

    tmp = requests.post(api_token, data={'grant_type': 'authorization_code',
                                         'code': aut_code,
                                         'redirect_uri': redirect_url},
                        headers={'Authorization': f'Basic {base64_string}',
                                 'Content-Type': 'application/x-www-form-urlencoded'})
    getdata(tmp.json())

    return "The playlist is added to your spotify"


def getdata(data):
    global songs
    header = {"Authorization": f"Bearer {data['access_token']}"}
    userData = requests.get(spot_api + "/me", headers=header).json()
    userId = userData['id']
    # create playlist
    playlist = requests.post(spot_api + "/users/" + userId + "/playlists", json={"name": listOfVideos[vidInd][0],
                                                                                 "public": "true",
                                                                                 "description": "aaa"
                                                                                 }, headers=header).json()
    playlistId = playlist['id']

    selectedTrack = []  # list of track ids from queried tracks
    for z in range(1, len(listOfVideos[vidInd]), 2):
        songs = requests.get(spot_api + "/search", {'q': listOfVideos[vidInd][z], 'type': "track", 'limit': 10},
                             headers=header).json()
        songitems = songs['tracks']['items']
        done = False

        for x in range(len(songitems)):
            for y in range(len(songitems[x]['artists'])):
                if songitems[x]['artists'][y]['name'].lower() not in listOfVideos[vidInd][z].lower():
                    break
                if y == len(songitems[x]['artists']) - 1:
                    if "live" not in listOfVideos[vidInd][z] and "live" in songitems[x]['name'].lower():
                        break

                    else:
                        selectedTrack.append(songitems[x]['uri'])
                        done = True
            if done:
                break

    # turn tracks to rows of 100
    updatedTrack = []
    if len(selectedTrack) > 100:
        rowlist = []
        for y in range(len(selectedTrack)):
            rowlist.append(selectedTrack[y])
            if y % 100 == 0:
                updatedTrack.append(rowlist)
                rowlist = []
        updatedTrack.append(rowlist)

        for x in range(math.ceil(len(selectedTrack) / 100)):
            addResponse = requests.post(spot_api + "/playlists/" + playlistId + "/tracks",
                                        json={"uris": updatedTrack[x]},
                                        headers=header)
            print(addResponse.text)

    else:
        addResponse = requests.post(spot_api + "/playlists/" + playlistId + "/tracks", json={"uris": selectedTrack},
                                    headers=header)
        print(addResponse.text)

    return songs


if __name__ == "__main__":
    main()
    app.run(debug=True)