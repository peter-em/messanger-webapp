'use strict'

var sounds = {
  "newmsg" : {
    url : "/sounds/hangouts.ogg"
  }
};


var soundContext = new AudioContext();

for(let key in sounds) {
  loadSound(key);
}

function loadSound(name){
  let sound = sounds[name];

  let url = sound.url;
  // var buffer = sound.buffer;

  let request = new XMLHttpRequest();
  request.open('GET', url, true);
  request.responseType = 'arraybuffer';

  request.onload = () => {
    soundContext.decodeAudioData(request.response, (newBuffer) => {
      sound.buffer = newBuffer;
    });
  }

  request.send();
}

function playSound(name, options){
  let sound = sounds[name];
  let soundVolume = sounds[name].volume || 1;

  let buffer = sound.buffer;
  if(buffer){
    let source = soundContext.createBufferSource();
    source.buffer = buffer;

    let volume = soundContext.createGain();

    if(options) {
      if(options.volume) {
        volume.gain.value = soundVolume * options.volume;
      }
    } else {
      volume.gain.value = soundVolume;
    }

    volume.connect(soundContext.destination);
    source.connect(volume);
    source.start(0);
  }
}
