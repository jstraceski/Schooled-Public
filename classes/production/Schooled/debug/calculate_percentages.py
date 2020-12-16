import subprocess, re
import os.path, json
import collections


def line_count(file_path):
    return sum(1 for _ in open(file_path))


fname = "percent_file.json"
root = "."
matches = []
for path, subdirs, files in os.walk(root):
    for name in files:
        if ".java" in name:
            matches += [[line_count(os.path.join(path, name)), name[:-5]]]

data = {}
loaded_data = None

if os.path.isfile(fname):
    data_file = open(fname, "r")
    data = json.load(data_file)
    data_file.close()

for n in matches:
    if n[1] in data:
        data[n[1]]['size'] = int(n[0])
    else:
        data.update({n[1]: {'size': int(n[0]), 'completed': 0, 'modified': False}})

output_file = open(fname, "w")
output_file.write(json.dumps(data, indent=2, sort_keys=True).replace("\r","\n"))
output_file.close()

total_lines = 0.0
completed_total = 0.0;
for n in data:
    total_lines += data[n]['size']
    completed_total += data[n]['size'] * data[n]['completed']
total = (float(completed_total) / float(total_lines))
print("%.2f%% completed." % total)

##############################################################################
#
# A simple example of some of the features of the XlsxWriter Python module.
#
# Copyright 2013-2019, John McNamara, jmcnamara@cpan.org
#
import xlsxwriter

# Create an new Excel file and add a worksheet.
workbook = xlsxwriter.Workbook('percent_complete.xlsx')
worksheet = workbook.add_worksheet()

# Widen the first column to make the textList clearer.
worksheet.set_column('B:B', 30)
worksheet.set_column('C:C', 15)
worksheet.set_column('D:D', 12)

# Add a bold format to use to highlight cells.
bold = workbook.add_format({'bold': True})
header = workbook.add_format({'bg_color': '#bfbfbf', 'border': True})
c_header = workbook.add_format({'bg_color': '#bfbfbf', 'border': True, 'align': 'center'})
file_style = workbook.add_format({'bg_color': '#f2f2f2', 'border': True})
comp_style = workbook.add_format({'num_format': '#0%', 'bg_color': '#f2f2f2', 'border': True, 'align': 'center'})
comp2_style = workbook.add_format({'num_format': '#0.#0%', 'bg_color': '#f2f2f2', 'border': True, 'align': 'center'})


worksheet.conditional_format('C3:C100', {'type': '3_color_scale',
                                         'min_value': 0,
                                         'min_type': 'num',
                                         'min_color': '#F8696B',
                                         'mid_value': 0.5,
                                         'mid_type': 'num',
                                         'mid_color': '#FFEB84',
                                         'max_value': 1,
                                         'max_type': 'num',
                                         'max_color': '#00FF99'
                                         })

worksheet.conditional_format('F3', {'type': '3_color_scale',
                                    'min_value': 0,
                                    'min_type': 'num',
                                    'min_color': '#F8696B',
                                    'mid_value': 0.5,
                                    'mid_type': 'num',
                                    'mid_color': '#FFEB84',
                                    'max_value': 1,
                                    'max_type': 'num',
                                    'max_color': '#00FF99'})

modi_style = workbook.add_format({'bg_color': '#f2f2f2', 'border': True, 'align': 'center'})
modi2_style = workbook.add_format({'bg_color': '#00FF99', 'border': True, 'align': 'center'})


# Text with formatting.
worksheet.write('B2', 'File', header)
worksheet.write('C2', 'Percent Complete', c_header)
worksheet.write('D2', 'Modified', c_header)

offset = 0
average = float(total_lines) / len(data)
# Write some numbers, with row/column notation.
od = collections.OrderedDict(sorted(data.items()))
for file in od:
    worksheet.write(2 + offset, 1, file, file_style)
    worksheet.write(2 + offset, 2, float(data[file]['completed']) / 100.0, comp_style)
    worksheet.write(2 + offset, 3, data[file]['size'] / float(total_lines) * 100, file_style)
    offset += 1

worksheet.write(1, 5, 'Total', c_header)
worksheet.write(2, 5, total/100.0, comp2_style)

workbook.close()
