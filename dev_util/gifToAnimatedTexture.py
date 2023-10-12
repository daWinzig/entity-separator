from PIL import Image
import tkinter as tk
from tkinter import filedialog
import os
import json

root = tk.Tk()
root.withdraw()

files = filedialog.askopenfilenames(filetypes=[("GIF",'*.gif')], initialdir=os.path.dirname(os.path.abspath(__file__)))
for file in files:
    with Image.open(file) as gif:
        frames = list()
        mcmeta = {"animation": {"frames": []}}
        for i in range(gif.n_frames):
            gif.seek(i)

            frame = Image.new("RGBA", gif.size)
            frame.paste(gif)

            index = len(frames)
            for j in range(len(frames)):
                if frames[j][0] == tuple(frame.getdata()):
                    index = j
            
            if index == len(frames):
                frames.append((tuple(frame.getdata()), frame))

            mcmeta["animation"]["frames"].append({
                    "index": index, 
                    "time": round(max(1, gif.info["duration"] / 50))
                })
            
        resultPath = os.path.splitext(file)[0] + ".png"

        with open(resultPath+".mcmeta", 'w') as f:
            json.dump(mcmeta, f, indent=2)

        result = Image.new("RGBA", (gif.width, gif.height*len(frames)))
        for i,frame in enumerate(frames):
            result.paste(frame[1], (0, i*gif.height))
        result.save(resultPath)