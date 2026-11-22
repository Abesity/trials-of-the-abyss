# Original chiptune music + SFX for Trials of the Abyss (written for this restoration).
import numpy as np, wave, sys, os
SR=22050
OUT=sys.argv[1]; os.makedirs(OUT,exist_ok=True)
def note(n): return 440*2**((n-69)/12)
def sq(f,d,duty=0.5,vol=0.25):
    t=np.arange(int(SR*d))/SR
    w=np.where((t*f)%1<duty,1.0,-1.0)*vol
    env=np.minimum(1,np.linspace(0,d*60,len(t)))*np.exp(-t*2.5)
    return w*env
def tri(f,d,vol=0.3):
    t=np.arange(int(SR*d))/SR; w=(2*np.abs(2*((t*f)%1)-1)-1)*vol
    return w*np.minimum(1,(d-t)*80)
def noise(d,vol=0.2,decay=20):
    t=np.arange(int(SR*d))/SR; return np.random.default_rng(1).uniform(-1,1,len(t))*vol*np.exp(-t*decay)
def mix(*parts):
    L=max(len(p) for p in parts); o=np.zeros(L)
    for p in parts: o[:len(p)]+=p
    return o
def seq(notes,beat,fn,**kw):
    o=[]
    for n,l in notes: o.append(np.zeros(int(SR*beat*l)) if n is None else fn(note(n),beat*l,**kw))
    return np.concatenate(o)
def save(name,x):
    x=np.clip(x,-1,1); d=(x*32767*0.8).astype('<i2')
    with wave.open(os.path.join(OUT,name),'wb') as w: w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR); w.writeframes(d.tobytes())
b=0.2
# overworld theme: A minor, adventurous, 16 bars of 8th notes
mel=[(69,2),(72,1),(76,1),(74,2),(72,2),(71,2),(72,1),(74,1),(76,4),(69,2),(72,1),(76,1),(79,2),(77,1),(76,1),(74,6),(None,2),
     (77,2),(76,1),(74,1),(72,2),(74,2),(76,2),(72,1),(69,1),(71,4),(72,2),(71,1),(69,1),(68,2),(71,2),(69,8)]
bass=[]
for root in [45,45,43,43,41,41,40,40,45,45,43,43,41,40,45,45]:
    bass+= [(root,1),(root+12,1),(root+7,1),(root+12,1)]
m=seq(mel,b,sq,duty=0.25,vol=0.18); bs=seq(bass,b,tri,vol=0.22)
hat=np.concatenate([noise(b,0.05,60) for _ in range(len(bass))])
save('overworld.wav',mix(m,bs,hat))
# battle theme: faster, driving
bb=0.13
bmel=[(76,1),(76,1),(79,1),(76,1),(81,2),(79,1),(77,1),(76,2),(74,2),(72,2),(74,2)]*2+[(77,1),(77,1),(81,1),(77,1),(84,2),(83,1),(81,1),(80,4),(83,4)]*2
bbass=[]
for r in [40,40,40,40,38,38,40,40,41,41,41,41,44,44,44,44]: bbass+=[(r,1),(r+12,1)]
bbass=bbass*2
save('battle.wav',mix(seq(bmel,bb,sq,duty=0.5,vol=0.16),seq(bbass,bb,sq,duty=0.125,vol=0.14),np.concatenate([noise(bb,0.07,45) if i%2==0 else np.zeros(int(SR*bb)) for i in range(len(bbass))])))
save('coin.wav',np.concatenate([sq(note(83),0.06,vol=0.3),sq(note(88),0.18,vol=0.3)]))
save('heal.wav',np.concatenate([sq(note(n),0.07,0.5,0.25) for n in (72,76,79,84,88)]))
save('unlock.wav',np.concatenate([noise(0.05,0.3,40),sq(note(60),0.06,0.5,0.25),sq(note(67),0.12,0.5,0.25)]))
save('fanfare.wav',np.concatenate([sq(note(n),l,0.5,0.25) for n,l in ((72,0.12),(72,0.12),(72,0.12),(76,0.36),(79,0.2),(76,0.12),(79,0.6))]))
save('hit.wav',mix(noise(0.15,0.4,25),sq(note(45),0.1,0.5,0.2)))
save('teleport.wav',np.concatenate([sq(note(n),0.035,0.25,0.2) for n in range(60,96,3)]))
print('ok')
