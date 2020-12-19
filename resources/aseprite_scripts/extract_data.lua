
function setup(spr)
	if not spr then return print('No active sprite') end

	kList = {"gl_l", "ga_l", "lo_l", "l", "global_layer", "game_layer", "local_layer", "gl_pos", "global_position", "lo_pos", "local_position", "ch_l", "child_layer"}

	kDict = {}

	kDict["gl_l"] = "global_layer"
	kDict["ga_l"] = "game_layer"
	kDict["lo_l"] = "local_layer"
	kDict["gl_pos"] = "global_position"
	kDict["lo_pos"] = "game_position"
	kDict["lo_l"] = "local_layer"
	kDict["l"] = "lookup"
	kDict["ch_l"] = "child_layer"
	kDict["child_layer"] = "child_layer"
	kDict["cpos"] = "child_pos"
	kDict["child_pos"] = "child_pos"
	kDict["global_layer"] = "global_layer"
	kDict["game_layer"] = "game_layer"
	kDict["local_layer"] = "local_layer"

	nList = {"global_layer", "game_layer", "local_layer", "child_layer"}

	boxesLookup = {}
	boxesData = {}
	SPR_PATH = spr.filename
	SPR_NAME = getFileName(SPR_PATH, SEPARATOR)
	SPR_DIR = getPath(SPR_PATH, SEPARATOR)
	RESOURCE_DIR = SPR_DIR:sub(1, SPR_DIR:find("resources") - 1).."resources"..SEPARATOR
	RESOURCE_DIR_LIST = scandirs(RESOURCE_DIR)
	JSON_FILE_LIST = scanJson(RESOURCE_DIR)
end

function getDelays(spr, tag)
	if tag ~= nil and tag.frames > 1 then
		local delays = {}

		for f_idx = tag.fromFrame.frameNumber, tag.toFrame.frameNumber do
			table.insert(delays, spr.frames[f_idx].duration)
		end

		return delays
	else
		return nil
	end
end

function lookupBoxTag(layer, tag, boxes)

	if tag ~= nil then
		indexes = {}

		b_idx = 1
		for f_idx = tag.fromFrame.frameNumber, tag.toFrame.frameNumber do
			local cbox = colapseChildren(boxes)
			if (boxes ~= nil and boxes[b_idx] ~= nil and cbox ~= {} and cbox[b_idx] ~= nil) then
				table.insert(indexes, lookupBox(layer:cel(f_idx).image, boxes[b_idx]))
				b_idx = b_idx + 1
			end
		end

		return indexes
	else
		indexes = {}
		table.insert(indexes, lookupBox(layer:cel(1).image, boxes))
		return indexes
	end
end

function arrEq(a, b)
	if type(a) == "table" and type(b) == "table" then
		for a_key, a_data in pairs(a) do
			if not arrEq(a_data, b[a_key]) then
				return false
			end
		end
	else
		return a == b
	end
end

function lookupBox(img, boxes)

	if (img == nil or img == {}) then
		return nil
	end

	for i,idata in ipairs(boxesLookup) do
		if (idata[1] == img or arrEq(idata[2], boxes)) then
			return i
		end
	end

	table.insert(boxesLookup, {img, boxes})
	table.insert(boxesData, boxes)

	return #boxesLookup
end

---------- ---------- ---------- ---------- ----------
---------- ---------- ---------- ---------- ----------
---------- ---------- ---------- ---------- ----------

function dirHelper(directory, ending)
		local i, t, popen, pfile = 0, {}, io.popen, ""

		f_text = "\"" .. directory .. ending .. "\""
		if pcall(function() popen("dir /B /S /A:D " .. f_text) end) then
				pfile = popen("dir /B /S /A:D " .. f_text)
		elseif pcall(function() popen('ls -a '..f_text) end) then
				pfile = popen('ls -a '..f_text)
		else
				os.execute('ls -a ' .. f_text .. ' > files.txt')
				pfile = assert(io.open("files.txt", "r"))
		end

		for filename in pfile:lines() do
				if filename:sub(1, 1) ~= "." then
					i = i + 1
					t[i] = filename
				end
		end
		pfile:close()
		return t
end

function scandirs(directory)
		return dirHelper(directory, '')
end


function scanJson(directory)
		return dirHelper(directory, SEPARATOR .. "*.json\"")
end

function getFileName(str,sep)
   --[[ Sources:
      - https://codereview.stackexchange.com/questions/90177/get-file-name-with-extension-and-get-only-extension
      - https://stackoverflow.com/questions/18884396/extracting-filename-only-with-pattern-matching
   --]]
   str = str:match("^.+"..sep.."(.+)$")
   return str:match("(.+)%..+")
end

function getPath(str,sep)
   -- Source: https://stackoverflow.com/questions/9102126/lua-return-directory-path-from-path
    sep=sep or'/'
    return str:match("(.*"..sep..")")
end

function writeToFile(f_name, d_text)
	file = io.open(f_name, "w")

	if file ~= nil then
		file:write(d_text)
		file:close()
	else
		print("Couldn't open file: " .. f_name);
	end
end

function getOnly(list, getList, covList)
	keys, is_list = getKeys(list)

	local collector = {}

	if is_list then
		for i1, d in ipairs(list) do

			local collector2 = {}

			for i2,k in ipairs(getList) do
				if (covList ~= nil) then
					collector2[covList[i2]] = list[i1][k]
				else
					collector2[k] = list[i1][k]
				end
			end

			table.insert(collector, collector2)
		end
	else

		for i,k in ipairs(getList) do

			if (covList ~= nil) then
				collector[covList[i]] = list[k]
			else
				collector[k] = list[k]
			end
		end
	end

	return collector
end

function getBoxCenters(layer, tag, boxData)
	if tag ~= nil then

		local boxes = {}
		local totals = {}
		for f_idx = tag.fromFrame.frameNumber, tag.toFrame.frameNumber do
			a, b = getBoxCentersHelper(layer:cel(f_idx), boxData)
			table.insert(boxes, a)
			table.insert(totals, b)
		end

		return boxes, totals

	else
		return getBoxCentersHelper(layer:cel(1), boxData)
	end
end

function getBoxCentersHelper(cel, boxData)
	local bList = {}

	if cel == nil then
		return {{}}, {}
	end

	local image = cel.image
	local colorMode = image.colorMode
	local redPixel = app.pixelColor.rgba(255, 0, 0)
	local clear = app.pixelColor.rgba(0, 0, 0, 0)

	local pos = cel.position

	local total = {}

	local w = image.width
	local h = image.height

	for y = 0, h - 1 do
		for x = 0, w - 1 do
			local cPix = image:getPixel(x, y)

			local uPix = y - 1 < 0  and clear or image:getPixel(x, y - 1)
			local dPix = y + 1 >= h and clear or image:getPixel(x, y + 1)
			local lPix = x - 1 < 0  and clear or image:getPixel(x - 1, y)
			local rPix = x + 1 >= w and clear or image:getPixel(x + 1, y)

			if (cPix ~= clear and lPix ~= cPix and uPix ~= cPix) then
				xedge = true
				yedge = true
				xoff = 0
				yoff = 0

				local bData = {}

				while (xedge or yedge) do
					if xedge then
						local nextValue = image:getPixel(x + xoff + 1, y)

						if nextValue ~= cPix or x + xoff + 1 >= w then
							xedge = false
						else
							xoff = xoff + 1
						end
					end
					if (not xedge and yedge) then
						local nextValue = image:getPixel(x, y + yoff + 1)

						if nextValue ~= cPix or y + yoff + 1 >= h then
							yedge = false
						else
							yoff = yoff + 1
						end
					end
				end

				bData["x"] = pos.x + x
				bData["y"] = pos.y + y
				bData["w"] = xoff + 1
				bData["h"] = yoff + 1
				bData["cx"] = bData["x"] + (bData["w"] / 2)
				bData["cy"] = bData["y"] + (bData["h"] / 2)


				total["x1"] = (total["x1"] == nil or bData["x"] <= total["x1"]) and bData["x"] or total["x1"]
				total["y1"] = (total["y1"] == nil or bData["y"] <= total["y1"]) and bData["y"] or total["y1"]

				total["x2"] = (total["x2"] == nil or bData["x"] + bData["w"] >= total["x2"]) and (bData["x"] + bData["w"]) or total["x2"]
				total["y2"] = (total["y2"] == nil or bData["y"] + bData["h"] >= total["y2"]) and (bData["y"] + bData["h"]) or total["y2"]


				bData["color"] = {app.pixelColor.rgbaR(cPix), app.pixelColor.rgbaG(cPix), app.pixelColor.rgbaB(cPix), app.pixelColor.rgbaA(cPix)}

				if (boxData ~= nil) then
					for k,v in pairs(boxData) do
						bData[k] = v;
					end
				end

				table.insert(bList, bData)
			end
		end
	end

	total["x"] = total["x1"]
	total["y"] = total["y1"]

	total["w"] = total["x2"] - total["x1"]
	total["h"] = total["y2"] - total["y1"]

	total["cx"] = ((total["x1"] + total["x2"]) / 2)
	total["cy"] = ((total["y1"] + total["y2"]) / 2)

	total["x1"] = nil
	total["y1"] = nil
	total["x2"] = nil
	total["y2"] = nil

	return bList, total;
end

function getKeys(tab)
	local keyset = {}
	is_array = true
	index = 0

	for k,v in pairs(tab) do
		if is_array and (tonumber(k) == nil or tonumber(k) <= 0 or index ~= #keyset)  then
			is_array = false;
		end

		keyset[#keyset + 1] = k
		index = index + 1
	end

	return keyset, is_array
end

function toJson(data_table)

	if (data_table == nil) then
		return "nil";
	end

	local stack = {}

	local json_str = ""
	local data = ""

	local keys, is_array = getKeys(data_table)

	if is_array then
		json_str = "["
	else
		json_str = "{"
	end

	local first_loop = true
	local eflag = true

	local level = 1

	local i1 = 1
	while i1 <= #keys or eflag do

		if i1 <= #keys then
			k1 = keys[i1]
			v1 = data_table[k1]

			data = ""

			if first_loop then
				if type(v1) == "string" then
					data = "\"" .. tostring(v1) .. "\""
				else
					data = tostring(v1)
				end

				header = ""

				if i1 == 1 then
					header = "\n"
					for i=1,level do header = header .. "    " end
				end

				if is_array then
					json_str = json_str .. header
				else
					json_str = json_str .. header .. "\"" .. tostring(k1) .. "\"" .. " : "
				end
			end
		end

		if i1 <= #keys and first_loop and (type(v1) == "table") then
			stack[#stack + 1] = {keys, data_table, i1, is_array}

			data_table = v1
			level = level + 1
			keys, is_array = getKeys(data_table)
			i1 = 0
			data = ""

			if is_array then
				json_str = json_str.."["
			else
				json_str = json_str.."{"
			end

		else
			footer = ",\n"
			for i=1,level do footer = footer .. "    " end

			first_loop = true

			if i1 >= #keys then
				last_array = is_array
				last_keys = keys
				eflag = (#stack > 0)

				if #stack == 0 then
					level = level - 1
				end

				if (#stack > 0) then
					stack_last = stack[#stack]
					stack[#stack] = nil

					keys = stack_last[1]
					data_table = stack_last[2]
					i1 = stack_last[3] - 1

					level = level - 1

					is_array = stack_last[4]
					first_loop = false
				end


				if #last_keys > 0 then
					footer = "\n"
					for i=1,level do footer = footer .. "    " end
				else
					footer = ""
				end

				if last_array then
					footer = footer.."]"
				else
					footer = footer.."}"
				end
			end
			json_str = json_str .. data .. footer
		end
		i1 = i1 + 1
	end

	return json_str
end

function addLists(l1, l2)

	if l1 == nil then
		return l2
	end

	if l2 == nil then
		return l1
	end

	keys1, is_array1 = getKeys(l1)
	keys2, is_array2 = getKeys(l2)

	if (is_array1 and is_array2) then
		for idx = 1,#l2 do
			l1[#l1 + 1] = l2[idx]
		end
	else
		for key,value in pairs(l2) do
			l1[key] = value
		end
	end


	return l1
end

function splitData(inputstr, sep, sep2)
	if sep == nil then
		sep = "%s"
	end
	local t={}
	for str in string.gmatch(inputstr, "([^"..sep.."]+)") do
		local out = string.gmatch(str, "([^"..sep2.."]+)")
		local key = out()
		local matchData = out()
		local nVersion = tonumber(matchData)

		if (nVersion ~= nil) then
			t[key] = nVersion
		else
			t[key] = matchData
		end

	end
	return t
end

function findLayer(sprite, sprite_str, lay_idx)
	local layer_list = sprite.layers

	if current_group ~= nil then
		layer_list = current_group.layers
	end

	local layers = sprite.layers
	for i, layer in ipairs(layer_list) do
		if (layer.name:lower() == sprite_str:lower()) then
			return layer, 0
		end
	end

	local n_layer = sprite:newLayer()

	if current_group ~= nil then
		n_layer.parent = current_group
	end

	sprite:newCel(n_layer, 1)
	n_layer.name = sprite_str
	n_layer.stackIndex = lay_idx

	return n_layer, 1
end

function addSprite(dest_img, s_img, d_pos, snip)

	local clear = app.pixelColor.rgba(0, 0, 0, 0)

	for it in s_img:pixels() do
		local pVal = it()

		local cull = false
		local xoff = 0
		local yoff = 0

		if snip ~= nil then
			if it.x < snip["x"] or it.y < snip["y"] or it.x > snip["x"] + snip["w"] or it.y > snip["y"] + snip["h"] then
				cull = true
			end

			xoff = snip["x"]
			yoff = snip["y"]
		end

		if (pVal ~= clear) and not cull then
			dest_img:drawPixel(it.x - xoff + d_pos.x, it.y - yoff + d_pos.y, pVal)
		end
	end
end

function drawSprites(dest_spr, tag, layer, e_list, s_img, spr_data)

	x_off = -spr_data["pos"]["x"]
	y_off = -spr_data["pos"]["y"]

	local cel = layer:cel(1)
	local n_cel = nil

	app.activeSprite = dest_spr

	if (cel ~= nil) then
		local l_img = cel.image:clone()
		local pos = cel.position

		n_cel = dest_spr:newCel(layer, 1)
		addSprite(n_cel.image, l_img, pos)
	else
		n_cel = dest_spr:newCel(layer, 1)
	end

	for i,data in ipairs(e_list) do
		addSprite(n_cel.image, s_img, Point(data["cx"] + x_off, data["cy"] + y_off))
	end
end

function readJson(file)
    local f = io.open(file, "rb")

	if (f == nil) then
		return nil
	end

    local content = f:read("*all")
    f:close()
    return json.decode(content)
end

function getSpriteData(level, tag, key)

	local l_collect = {}

	for idx, layer in ipairs(level.layers) do
		if (layer.layers ~= nil) then
			table.insert(l_collect, layer)
		end

		if (layer.name:sub(1,6) == "[data]") then
			return dLookup(layer, tag, key)
		end
	end

	for idx, layer in ipairs(l_collect) do
		getSpriteData(layer, tag, key)
	end
end


function getJsonSprite(json_path, json_data, layer_data)
	local data_path = getPath(json_path, SEPARATOR)
	local json_spr_path = data_path .. SEPARATOR .. json_data["sheet"]
	local active_spr = app.activeSprite

	local i_data = json_data["images"]
	local s_data = i_data["#base"]

	if s_data == nil then
		local k, arr = getKeys(i_data)
		s_data = i_data[k[1]]
	end

	local k, arr = getKeys(s_data)
	local p_data = splitData(layer_data, ",", ":")

	if not arr then
		s_data = s_data["states"]
		local k, arr = getKeys(s_data)

		if p_data["state"] ~= nil then
			s_data = s_data[p_data["state"]]
		else
			s_data = s_data[k[1]]
		end
	end

	local l_list = {}

	local h_idx = 1
	local l_idx = 1

	for i_idx, d_data in ipairs(s_data) do
		c_idx = d_data["local_layer"]
		if c_idx == nil then
			c_idx = 0
		end

		if c_idx >= h_idx then
			h_idx = c_idx
		end

		if c_idx <= l_idx then
			l_idx = c_idx
		end

		if l_list[c_idx] == nil then
			l_list[c_idx] = {}
		end

		table.insert(l_list[c_idx], d_data["image"])
	end

	local nSpr = nil
	local nImg = nil
	local sprite = app.open(json_spr_path)
	local sImg = sprite.layers[1]:cel(1).image:clone()

	for i_idx = l_idx, h_idx do
		if l_list[i_idx] ~= nil then
			for l1, n_i_data in ipairs(l_list[i_idx]) do
				local d, isarr = getKeys(n_i_data)

				if isarr then
					n_i_data = n_i_data[1]
				end

				if nImg == nil then
					nImg = Image(n_i_data["w"], n_i_data["h"])
				end

				addSprite(nImg, sImg, Point(0,0), n_i_data)
			end
		end
	end

	sprite:close()

	return nImg, json_data
end


function findSprite(spr_str, path, layer_data)
	for idx, json_path in ipairs(JSON_FILE_LIST) do
		local json_data = readJson(json_path)

		local str = json_data["lookup"]

		if str ~= nil then
			str = str:gsub("%s+", "")
			str = str:lower()
		end

		if str == spr_str then
			a,b = getJsonSprite(json_path, json_data, layer_data)

			if (a ~= nil and b ~= nil) then
				return a,b
			end

		end
	end


	for idx, sub_path in ipairs(RESOURCE_DIR_LIST) do
		a,b = findSpriteSub(spr_str, sub_path .. SEPARATOR, "-")
		if (a ~= nil and b ~= nil) then
			return a,b
		end
	end

	if (a == nil or b == nil) then
		print("couldn't find json " .. spr_str)
	end

	return nil, nil
end

function addBaseBox(spr, tag, boxIdx, areaType)

	if tData[areaType] == nil then
		tData[areaType] = {}
	end

	if tData[areaType]["#base"] == nil then
		tData[areaType]["#base"] = {}
	end

	if tag ~= nil then

		if tData[areaType]["#base"]["states"] == nil then
			tData[areaType]["#base"]["states"] = {}
		end

		if tData[areaType]["#base"]["states"][tag.name] == nil then
			tData[areaType]["#base"]["states"][tag.name] = {}
		end

		tData[areaType]["#base"]["states"][tag.name]["ids"] = boxIdx

		local out = getDelays(spr, tag)

		if out ~= nil then
			tData[areaType]["#base"]["states"][tag.name]["delays"] = out
		end
	else

		tData[areaType]["#base"]["ids"] = boxIdx

		local out = getDelays(spr, tag)

		if out ~= nil then
			tData[areaType]["#base"]["delays"] = out
		end
	end
end

function findSpriteSub(tag, path, sep)
	json_tag = sep .. "edata.json"

	file = io.open(path .. tag .. ".png", "r")

	sprite = nil
	if file ~= nil then
		sprite = app.open(path .. tag .. ".png")
	else
		return nil, nil
	end

	local tag = tag:lower()

	if (sprite ~= nil) then
		local jsonData = readJson(path .. tag .. json_tag)
		img = sprite.layers[1]:cel(1).image:clone()
		sprite:close()
		return img, jsonData
	end



	-- for i,sprite in ipairs(app.sprites) do
		-- local spritePath = sprite.filename
		-- local spriteName = getFileName(spritePath, SEPARATOR)

		-- lookup_tag = getSpriteData(sprite, tag, "lookup")

		-- if (spriteName:lower() == tag) then
			-- local jsonData = readJson(path .. spriteName .. json_tag)
			-- return sprite, jsonData
		-- end
	-- end

	return nil, nil

	-- spr = app.open(path)
	-- spr:close()
end

function entityRouter(spr, layer, tag, i)
	local lay_spr_str = layer.name:sub(9,-2)
	local eData = splitData(layer.data, ",", ":")
	eData["visible"] = nil
	eData["entity"] = lay_spr_str
	local e_list_add = getBoxCenters(layer, tag, eData)

	e_list = addLists(e_list, e_list_add)

	if (not dLookup(layer, tag, "local", "true")) then
		local lay_spr, lay_spr_data = findSprite(lay_spr_str, SPR_DIR, layer.data)

		if (lay_spr ~= nil and lay_spr_data ~= nil) then
			draw_layer, off = findLayer(spr, lay_spr_str, i + i_off)
			i_off = i_off + off
			draw_layer.isVisible = false
			drawSprites(spr, tag, draw_layer, e_list_add, lay_spr, lay_spr_data)
		end
	end
end



function avgArea(box)
	local kl, isArr = getKeys(box)

	if isArr then
		for idx, data in ipairs(box) do
			if (data ~= {} and data ~= nil and data["x"] ~= nil) then
				totalBox["x1"] = (totalBox["x1"] == nil or data["x"] <= totalBox["x1"]) and data["x"] or totalBox["x1"]
				totalBox["y1"] = (totalBox["y1"] == nil or data["y"] <= totalBox["y1"]) and data["y"] or totalBox["y1"]

				totalBox["x2"] = (totalBox["x2"] == nil or data["x"] + data["w"] >= totalBox["x2"]) and (data["x"] + data["w"]) or totalBox["x2"]
				totalBox["y2"] = (totalBox["y2"] == nil or data["y"] + data["h"] >= totalBox["y2"]) and (data["y"] + data["h"]) or totalBox["y2"]
			end
		end
	else
		if (box ~= {} and box ~= nil and box["x"] ~= nil) then
			totalBox["x1"] = (totalBox["x1"] == nil or box["x"] <= totalBox["x1"]) and box["x"] or totalBox["x1"]
			totalBox["y1"] = (totalBox["y1"] == nil or box["y"] <= totalBox["y1"]) and box["y"] or totalBox["y1"]

			totalBox["x2"] = (totalBox["x2"] == nil or box["x"] + box["w"] >= totalBox["x2"]) and (box["x"] + box["w"]) or totalBox["x2"]
			totalBox["y2"] = (totalBox["y2"] == nil or box["y"] + box["h"] >= totalBox["y2"]) and (box["y"] + box["h"]) or totalBox["y2"]
		end
	end
end


function route(spr, i, layer, tag)
	if layer.isVisible then
		table.insert(visible_layers, layer)
	end

	if (dLookup(layer, tag, "visible", "false") or layer.name:sub(1,1) == "[") then
		layer.isVisible = false
	else
		layer.isVisible = true
	end

	if (layer.layers ~= nil or hasData(layer, tag)) then
		table.insert(group_group_children, layer)
		layer.isVisible = false
	elseif layer.isVisible then
		table.insert(group_visible_layers, layer)
	end

	if (layer.name:sub(1,6) == "[wall]") then
		local bDataOut, bc = getBoxCenters(layer, tag)
		local out = lookupBoxTag(layer, tag, bDataOut)
		addBaseBox(spr, tag, out, "walls")
		tData["type"] = "room"

	elseif (layer.name:sub(1,8) == "[entity:") then
		entityRouter(spr, layer, tag, i)

	elseif (layer.name:sub(1,6) == "[area:") then
		local lay_spr_str = layer.name:sub(7,-2)
		local bDataOut = getBoxCenters(layer, tag)
		a_list[lay_spr_str] = lookupBoxTag(layer, tag, bDataOut)

	elseif (layer.name:sub(1,4) == "[box") then
		local bDataOut, bc = getBoxCenters(layer, tag)
		avgArea(bc)


		addBaseBox(spr, tag, lookupBoxTag(layer, tag, bDataOut), "areas")
		-- data["pos"] = getOnly(bc, {"cx", "cy"})

	elseif (layer.name:sub(1,4) == "[pos") then
		lst, bc = getBoxCenters(layer, tag)
		local only = getOnly(bc, {"cx", "cy"}, {"x", "y"})
		local ks, isArr = getKeys(only)

		if (isArr) then
			tData["pos"] = only[1]
		else
			tData["pos"] = only
		end

	elseif (layer.name:sub(1,6) == "[data]") then
		tData = addLists(tData, splitData(layer.data, ",", ":"))

	elseif (layer.name:sub(1,5) == "[lbox") then
		lst, bc = getBoxCenters(layer, tag)
		if (bc ~= nil and next(bc) ~= nil) then
			group_box = getOnly(bc, {"w", "h", "x", "y"})

			local none, isArr = getKeys(group_box)

			if isArr and #group_pos < 2 then
				group_box = group_box[1]
			end
		end

	elseif (layer.name:sub(1,5) == "[lpos") then
		lst, bc = getBoxCenters(layer, tag)
		if (bc ~= nil and next(bc) ~= nil) then
			group_pos = getOnly(bc, {"cx", "cy"}, {"x", "y"})

			local none, isArr = getKeys(group_pos)

			if isArr and #group_pos < 2 then
				group_pos = group_pos[1]
			end
		end

	elseif (layer.name:sub(1,5) == "[cpos") then
		lst, bc = getBoxCenters(layer, tag)
		if (bc ~= nil and next(bc) ~= nil) then
			child_pos = getOnly(bc, {"cx", "cy"}, {"x", "y"})

			local none, isArr = getKeys(child_pos)

			if isArr and #child_pos < 2 then
				child_pos = child_pos[1]
			end
		end

	elseif (layer.name:sub(1,5) == "[gpos") then
		lst, bc = getBoxCenters(layer, tag)
		if (bc ~= nil and next(bc) ~= nil) then
			game_pos = getOnly(bc, {"cx", "cy"}, {"x", "y"})

			local none, isArr = getKeys(game_pos)

			if isArr and #game_pos < 2 then
				game_pos = game_pos[1]
			end
		end

	end

end

function hasData(lay, tag)
	for i_idx, k in ipairs(kList) do
		local ret = dLookup(lay, tag, k)

		if ret ~= nil and ret ~= false then
			return true
		end
	end

	return false
end

function dLookup(lay, tag, key, val)
	if tag ~= nil and lay ~= nil and lay.__name == "Layer" then
		for c_idx = tag.fromFrame.frameNumber, tag.toFrame.frameNumber do
			local ret = cLookup(lay:cel(c_idx), key, val)

			if ret ~= nil and ret ~= false then
				return ret
			end
		end
	end

	return cLookup(lay, key, val)
end

function cLookup(lay, key, val)
	if (lay == nil) then
		return nil
	end

	if (lay.__name ~= "doc::Sprite") and type(lay) == "userdata" then
		if val == nil then
			return splitData(lay.data, ",", ":")[key]
		else
			return splitData(lay.data, ",", ":")[key] == val
		end
	end

	return nil
end

function setVisibility(group, state)
	if type(group) == "userdata" then
		if (group.__name == "Layer") then
			group.isVisible = state
			if (group.parent ~= nil) then
				setVisibility(group.parent, state)
			end
		end
	else
		for g,glayer in ipairs(group) do
			glayer.isVisible = state
		end
	end
end

function sCopy(orig)
    local orig_type = type(orig)
    local copy
    if orig_type == 'table' then
        copy = {}
        for orig_key, orig_value in pairs(orig) do
            copy[orig_key] = orig_value
        end
    else -- number, string, boolean, etc
        copy = orig
    end
    return copy
end

function saveLayerSprite(tag, lay)
	local sdata = nil

	local layer_name = lay.__name == "Layer" and lay.name or nil

	if layer_name == nil or lay.name:sub(1,1) ~= "[" then
		sdata = saveSprite(SPR_DIR, SPR_NAME, tag, layer_name)
	end

	return sdata
end

function parseChildren(spr, tag)

	local children = {}
	if #group_group_children > 0 then
		for i2,child in ipairs(sCopy(group_group_children)) do

			table.insert(children, groupRouter(spr, child, tag, false))
		end
	end

	if #children > 0 then
		return children
	else
		return nil
	end
end


function addData(store, key1, data, key2)

	if type(data) == "table" then
		if next(data) ~= nil then
			if key2 ~= nil then
				store[key1] = addLists(store[key1], data[key2])
			else
				store[key1] = addLists(store[key1], data)
			end
		end
	else
		if data ~= nil then
			store[key1] = addLists(store[key1], key2 ~= nil and data[key2] or data)
		end
	end

	return store
end

function setData(store, key1, data, key2)

	if type(data) == "table" then
		if next(data) ~= nil then
			if key2 ~= nil then
				store[key1] = data[key2]
			else
				store[key1] = data
			end
		end
	else
		if data ~= nil then
			store[key1] = key2 ~= nil and data[key2] or data
		end
	end

	return store
end

function objFilter(data)
	if data == nil then
		return nil
	end

	if type(data) == "table" then
		local nData = {}
		local empty = true

		for k,v in pairs(data) do
			local vOut = objFilter(v)

			if vOut ~= nil then
				empty = false
				nData[k] = vOut
			end
		end

		if (not empty) then
			return nData
		else
			return nil
		end
	end

	return data
end

function groupRouter(spr, group_layer, tag, top)
	current_group = group_layer

	group_cell_children = {}
	group_group_children = {}
	group_visible_layers = {}

	group_box = {}
	group_pos = {}
	child_pos = {}
	game_pos = {}
	i_off = 0

	setVisibility(group_layer, true)

	decode(group_layer, spr, tag)

	local layer_data = setData({}, "image", addImage(spr, tag))

	setVisibility(group_visible_layers, false)

	for k_idx, k in ipairs(kList) do
		layer_data = setData(layer_data, kDict[k], dLookup(group_layer, tag, k))
	end

	for n_idx, n in ipairs(nList) do

		if layer_data[n] ~= nil then
			layer_data = setData(layer_data, n, tonumber(layer_data[n]))
		end
	end

	if layer_data["image"] ~= nil then
		layer_data = setData(layer_data, "delays", getDelays(spr, tag))
		layer_data["added"] = false
	end


	setVisibility(group_layer, false)

	layer_data = setData(layer_data, "local_pos", group_pos)
	layer_data = setData(layer_data, "child_pos", child_pos)
	layer_data = setData(layer_data, "layer_bounds", group_box)
	layer_data = setData(layer_data, "game_pos", game_pos)
	if type(group_layer) == "userdata" and layer_data["image"] ~= nil then
		if (group_layer.__name == "Layer") then
			layer_data = setData(layer_data, "layer_name", group_layer.name)
		end
	end

	layer_data = setData(layer_data, "children", parseChildren(spr, tag))


	if layer_data["children"] == nil and layer_data["image"] == nil then
		return nil
	end

	local lUp = dLookup(group_layer, tag, "lookup")

	local outData = colapseChildren(objFilter(layer_data))

	if (outData ~= nil and outData ~= {} and lUp ~= nil) then
		addSpriteData(outData, tag, lUp);
	end

	if (lUp ~= nil) then
		return nil
	end

	return outData
end

function hasImage(layer, tag)
	for a,b in ipairs(layer.layers) do
		local cel_data = nil
		if tag == nil then
			cel_data = b:cel(1)
		else
			cel_data = b:cel(tag.fromFrame.frameNumber)
		end

		if b.isVisible and cel_data ~= nil and not cel_data.image:isEmpty() then
			return true
		end

		if b.isVisible and b.layers ~= nil and hasImage(b, tag) then
			return true
		end
	end

	return false
end

function saveSprite(tag)


	if hasImage(tag) then

		return name
	end

	return nil
end

function getFileExt(spr, tag)
	tag_name = ""
	tag_file_ext = ""
	if (tag ~= nil) then
		tag_name = tag.name
		tag_file_ext = "-" .. tag_name
	end

	return tag_file_ext
end

function restoreVisibility()
	for key, layer in ipairs(visible_layers) do
		layer.isVisible = true
	end

	for key, layer in ipairs(aseprite_only_layers) do
		layer.isVisible = true
	end
end

function decode(item, spr, tag)
	if (item.layers ~= nil) then
		for i, layer in ipairs(item.layers) do
			route(spr, i, layer, tag)
		end
	end
end

function putAreas(spr, tag)
	k_list = getKeys(a_list)

	for idx, key in ipairs(k_list) do
		if tData["areas"] == nil then
			tData["areas"] = {}
		end

		if tag ~= nil then
			if tData["areas"][key] == nil then
				tData["areas"][key] = {}
			end

			if tData["areas"][key]["states"] == nil then
				tData["areas"][key]["states"] = {}
			end

			if tData["areas"][key]["states"][tag.name] == nil then
				tData["areas"][key]["states"][tag.name] = {}
			end

			tData["areas"][key]["states"][tag.name]["ids"] = a_list[key]

			local out = getDelays(spr, tag)

			if out ~= nil then
				tData["areas"][key]["states"][tag.name]["delays"] = out
			end
		else
			if tData["areas"][key] == nil then
				tData["areas"][key] = {}
			end

			tData["areas"][key]["ids"] = a_list[key]
		end
	end
end


function colapseChildren(indata)
	keys = getKeys(indata)

	local hasData = false

	for i,k in ipairs(keys) do
		if k ~= "children" and k ~= "delays" and k ~= "added" then
			hasData = true
		end
	end

	if (indata["children"] ~= nil and (not hasData)) then
		 indata = indata["children"]
	end

	if indata["children"] ~= nil then
		local outdata = {}

		local nData = sCopy(indata)
		nData["children"] = nil

		table.insert(outdata, nData)

		for i,d in ipairs(indata["children"]) do
			table.insert(outdata, d)
		end

		indata = outdata
	end

	keys, isArr = getKeys(indata)

	if isArr and #keys == 1 then
		indata = indata[1]
	end


	keys, isArr = getKeys(indata)

	if isArr then
		local outdata = {}
		for i, e in ipairs(indata) do
			keys, isArr = getKeys(e)

			if isArr then
				for i2, e2 in ipairs(e) do
					table.insert(outdata, e2)
				end
			else
				table.insert(outdata, e)
			end
		end

		indata = outdata
	end


	return indata
end

function removeAdded(indata)
	if indata == nil then
		return nil
	end


	indata["added"] = nil

	local ks = getKeys(indata)

	for i, k in ipairs(ks) do
		if type(indata[k]) == "table" then
			indata[k] = removeAdded(indata[k])
		end
	end

	return indata
end

function topAdded(indata)
	if indata == nil then
		return nil
	end

	local ks, is_array = getKeys(indata)
	if is_array then
		for i, k in ipairs(ks) do
			if not indata[k]["added"] then
				return false
			end
		end
	else
		if indata["added"] == nil then
			return false
		end

		return indata["added"]
	end

	return true
end

function addSpriteData(input, tag, name)
	local iarr = false
	local k = 0

	if input ~= nil then
		k,iarr = getKeys(input)
	end

	if iarr then
		for i, k in ipairs(k) do
			if not topAdded(input[k]) or tag ~= nil then
				ndata = removeAdded(sCopy(input[k]))

				if i_list[name] == nil then
					i_list[name] = {}
				end

				if tag ~= nil then

					if i_list[name]["states"] == nil then
						i_list[name]["states"] = {}
					end

					if i_list[name]["states"][tag.name] == nil then
						i_list[name]["states"][tag.name] = {}
					end

					if (ndata == nil and i_list[name]["states"][tag.name] == {}) then
						ndata = {}
						ndata["image"] = {}
						print("B")
					end

					table.insert(i_list[name]["states"][tag.name], ndata)
				elseif i_list[name] ~= nil then
					table.insert(i_list[name], ndata)
				end
				input[k]["added"] = true
			end
		end
	elseif not topAdded(input) or tag ~= nil then
		ndata = removeAdded(sCopy(input))

		if i_list[name] == nil then
			i_list[name] = {}
		end

		if tag ~= nil then
			if i_list[name]["states"] == nil then
				i_list[name]["states"] = {}
			end

			if i_list[name]["states"][tag.name] == nil then
				i_list[name]["states"][tag.name] = {}
			end

			if (ndata == nil and i_list[name]["states"][tag.name] == {}) then
				ndata = {}
				ndata["image"] = {}
			end

			table.insert(i_list[name]["states"][tag.name], ndata)
		elseif i_list[name] ~= nil then
			table.insert(i_list[name], ndata)
		end

		if (input ~= nil) then
			input["added"] = true
		end
	end
end

function parseSprite(spr, layer, tag, top)
	current_group = nil

	data = {}
	a_list = {}
	l_list = {}

	data = groupRouter(spr, layer, tag, top)


	if a_list ~= {} then
		putAreas(spr, tag)
	end

	if data == nil and tag == nil then
		return nil
	end


	addSpriteData(data, tag, "#base")


	return data
end

function finish(dlg, spr)

	aseprite_only_layers = {}
	visible_layers = {}

	lNum = getLayerCount(spr)
	fNum = #spr.frames
	local swap = false;


	if (swap) then
		local temp = fNum;
		fNum = lNum;
		lNum = temp;
	end

	sheetImage = Image(spr.width*fNum, spr.height*lNum)
	count = 0

	totalBox = {}

	tData = {}

	w_list = {}
	e_list = {}

	tData["type"] = "edata"
	i_list = {}

	if #spr.tags > 0 then
		for i,tag in ipairs(spr.tags) do
			parseSprite(spr, spr, tag, true)
		end
	else
		parseSprite(spr, spr, nil, true)
	end

	tData = setData(tData, "images", i_list)
	tData = setData(tData, "shape_lookup", boxesData)


	if tData["lookup"] == nil then
		tData["lookup"] = SPR_NAME;
	end


	if (tData["pos"] == nil) then
		kl, isarr = getKeys(totalBox)
		if (#kl < 1) then
			tData["pos"] = {x = 0, y = 0}
		else
			tData["pos"] = {x = ((totalBox["x1"] + totalBox["x2"]) / 2), y = ((totalBox["y1"] + totalBox["y2"]) / 2)}
		end
	end


	if (tData["size"] == nil) then
		tData["size"] = {w=spr.width, h=spr.height}
	end

	local wIdx = fNum
	local hIdx = math.floor((count - 1) / fNum) + 1

	fitSheetImage = Image(spr.width*wIdx, spr.height*hIdx)
	fitSheetImage:drawImage(sheetImage,  Point(0, 0))
	fitSheetImage:saveAs(SPR_DIR .. SEPARATOR .. SPR_NAME .. ".png")

	tData["sheet"] = SPR_NAME .. ".png"
	tData = setData(tData, "entities", e_list)
	tData = setData(tData, "walls", w_list)

	writeToFile(SPR_DIR .. SEPARATOR .. SPR_NAME .. "-edata.json", toJson(tData))

	restoreVisibility()

	app.activeSprite = spr

	app.command.SaveFile()
end

---------- ---------- ---------- ---------- ----------
---------- ---------- ---------- ---------- ----------
---------- ---------- ---------- ---------- ----------

function searchSubfolders(path, prefix)
	if (prefix == nil) then
		prefix = ""
	end

	for n, folder in ipairs(scandir(path)) do
		print(prefix .. folder)
		searchSubfolders(path .. SEPARATOR .. folder, prefix .. "\t")
	end
end

---------- ---------- ---------- ---------- ----------
---------- ---------- ---------- ---------- ----------
---------- ---------- ---------- ---------- ----------

function getLayerCount(level)
	local count = 0
	for idx, lay in ipairs(level.layers) do
		if #lay.cels > 0 then
			count = count + 1
		end

		if lay.layers ~= nil then
			count = count + getLayerCount(lay)
		end
	end

	return count
end

function addImage(spr, tag)
	if not hasImage(spr, tag) then
		return nil
	end

	local p_out = nil

	local sIdx = 1
	local eIdx = 1
	local single = true

	if tag ~= nil then
		sIdx = tag.fromFrame.frameNumber
		eIdx = tag.toFrame.frameNumber
	end

	for fIdx = sIdx, eIdx do
		local wIdx = count % fNum
		local hIdx = math.floor(count / fNum)

		local p = Point(spr.width * wIdx, spr.height * hIdx)
		sheetImage:drawSprite(spr, fIdx,  p)

		local json_format = {x = p.x, y = p.y, w = spr.width, h = spr.height}

		if p_out == nil then
			p_out = json_format
		elseif single then
			local p_old = p_out
			p_out = {}
			table.insert(p_out, p_old)
			table.insert(p_out, json_format)
			single = false
		else
			table.insert(p_out, json_format)
		end
		count = count + 1
	end

	return p_out
end

------------
-- WIZARD --
------------

local function div(code)
end

local function cancelWizard(dlg)
  dlg:close()
end

local function runWizard(dlg)

	local dlgData = dlg.data
	dlg:close()

	if (dlgData.radio11) then
		spr = app.activeSprite
		setup(spr)
		finish(dlg, spr)
	else
		for i,sprite in ipairs(app.sprites) do
			app.activeSprite = sprite
			setup(sprite)
			finish(dlg, sprite)
		end
	end

end

dlg1 = Dialog()
dlg1:radio{id="radio11", text="&Current", selected=true }
dlg1:radio{id="radio12", text="&All", selected=false }
dlg1:separator()
dlg1:button{ text="&Cancel", onclick= function() cancelWizard(dlg1) end }
dlg1:button{ text="&Generate", onclick= function() runWizard(dlg1) end }

dlg1:show{ wait=false }
