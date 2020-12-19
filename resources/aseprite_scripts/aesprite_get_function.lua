function getPath(str,sep)
   -- Source: https://stackoverflow.com/questions/9102126/lua-return-directory-path-from-path
    sep=sep or'/'
    return str:match("(.*"..sep..")")
end

SEPARATOR = "/"
SPR_PATH = app.activeSprite.filename
SPR_DIR = getPath(SPR_PATH, SEPARATOR)
if SPR_DIR == nil then
    SEPARATOR = "\\"
    SPR_DIR = getPath(SPR_PATH, SEPARATOR)
end
RESOURCE_DIR = SPR_DIR:sub(1, SPR_DIR:find("resources") - 1).."resources"..SEPARATOR

json = dofile(RESOURCE_DIR .. "aseprite_scripts" .. SEPARATOR .. "json.lua")
dofile(RESOURCE_DIR .. "aseprite_scripts" .. SEPARATOR .. "extract_data.lua")