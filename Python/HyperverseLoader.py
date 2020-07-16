import io


glb_file = open("Duck.glb", "rb")
data = io.BytesIO(glb_file.read())

print(type(data))



#stream = io.BytesIO(requests.get("https://github.com/KhronosGroup/glTF-Sample-Models/raw/master/2.0/Duck/glTF-Binary/Duck.glb").content)


#stream.seek(0)

#stream_data = stream.read(12)

#print(type(stream_data))
#print(stream_data)



#load("Duck.glb")
#print(value)