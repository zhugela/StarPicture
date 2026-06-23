import os, glob
d = r'D:/code/StarPicture/docs/test/'
for f in glob.glob(d + '*'):
    try:
        os.remove(f)
        print('removed:', os.path.basename(f))
    except Exception as e:
        print('skip:', os.path.basename(f), e)
# 删除 docs/test/ 下的子目录（之前没有）
for sub in glob.glob(d + '*/'):
    try:
        os.rmdir(sub)
    except: pass
print('remaining:', os.listdir(d))
