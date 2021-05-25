import random

for i in range(1000001, 1000501):
    nombre = random.choice(open("el_quijote_completo.txt","r").readline().split())
    autor = random.choice(open("el_quijote_completo.txt","r").readline().split())
    editorial = random.choice(open("el_quijote_completo.txt","r").readline().split())
    ISBN = str(random.randint(0,9999)) + "-" + str(random.randint(0,9999)) + "-" + str(random.randint(0,9999)) + "-" + str(random.randint(0,9999))
    fechaPub = str(random.randint(10,31)) + "/mayo/" + str(random.randint(1960, 2021))
    idiomas = ['ingles', 'espanol', 'frances', 'aleman', 'italiano', 'portuges', 'mandarin']
    categorias = ['drama', 'comedia', 'fantasia', 'terror', 'accion', 'novela', 'relgion', 'cuento', 'poesia', 'medieval']
    ejemplares = random.randint(1,10)
    print(i, ",", nombre, ",", autor, ",", ISBN, ",", editorial, ",", random.choice(idiomas), ",", random.choice(categorias), ",", "true", ",", ejemplares, ",", fechaPub)
