function getPath(str,sep)
   -- Source: https://stackoverflow.com/questions/9102126/lua-return-directory-path-from-path
    sep=sep or'/'
    return str:match("(.*"..sep..")")
end

SEPERATOR = "/"
SPR_PATH = app.activeSprite.filename
SPR_DIR = getPath(SPR_PATH, SEPERATOR)
RESOURCE_DIR = SPR_DIR:sub(1, SPR_DIR:find("resources") - 1).."resources"..SEPERATOR

json = dofile(RESOURCE_DIR .. "aseprite_scripts" .. SEPERATOR .. "json.lua")
dofile(RESOURCE_DIR .. "aseprite_scripts" .. SEPERATOR .. "extract_data.lua")