# INSTRUCTIONS:
# to use, type "python codegen.py Classname" where "Classname" is the name of the class you want. It will generate all the templated code.

# define data for each class: ( ((type,prop),(type,prop),...), (collection, collection, ...) )

classes = {
'Asset': ((('String','description'),('String','filename'),('String','title'),('String','thumbUrl'),('String','type'),('String','url')), ('materials',)), 
'CustomField': ((('Worksite','worksite'),('String','name'),('Integer','ordinalValue'),('Integer','visibility')), ('values',)), 
'CustomFieldValue': ((('CustomField','customField'),('Material','material'),('Integer','ordinalValue'),('String','value')), ()), 
'Material': ((('Asset','asset'),('Worksite','worksite'),('Integer','accessLevel'),('String','clipBegin'),('String','clipEnd'),('Date','dateModified'),('String','text'),('String','type')), ('customFieldValues',)), 
'Worksite': ((('String','announcement'),('String','courseIdString'),('String','title')), ('customFields','materials',))
}

def render_template(template_file,data):
    from htmltmpl import TemplateManager, TemplateProcessor

    mgr = TemplateManager()
    template = mgr.prepare(template_file)
    tproc = TemplateProcessor(global_vars=1)
    
    for key in data.keys():
        tproc.set(key,data[key])

    print tproc.process(template)


if __name__ == "__main__":
    import sys, os
    classname = sys.argv[1:][0]

    try:
        names = os.listdir('templates')
    except os.error:
        print 'ERROR READING DIRECTORY ****************'
        sys.exit()

    c = classes[classname][0]
    sets = classes[classname][1]
    datapack = {};
    datapack['class'] = classname
    datapack['lcclass'] = classname[0].lower() + classname[1:]
    datapack['props'] = []
    datapack['sets'] = []
    for prop in c:
        datapack['props'].append({'type':prop[0],'prop':prop[1],'ucprop':prop[1][0].upper()+prop[1][1:]})
    
    for setname in sets:
        datapack['sets'].append({'interface':'Set','typeImpl':'HashSet','prop':setname,'ucprop':setname[0].upper()+setname[1:]})
    
    for name in names:
        if name[-4:] == 'tmpl':
            print '\n\n\n=====================' + name + '========================\n\n\n'
            render_template('templates/' + name, datapack)
