my_text_file = ['quiz1.txt', 'quiz2.txt', 'quiz3.txt', 'quiz4.txt', 'quiz5.txt', 'quiz6.txt', 'quiz7.txt', 'quiz8.txt']

for i in range(len(my_text_file)):
  f = open(my_text_file[i], encoding='utf-8')

  quiz1 = []
  answer1 = []
  lines = f.readlines()
  for line in lines:
    param = line.split('=')
    #print(param[0].strip().lstrip('.'))

    #answer
    param2 = param[1].strip().lstrip('\n')
    param2 = param2.split("·")

    a=[]
    # ''인 요소 제거
    for item in param2:
      if item != '':
        a.append(item)
    
    # ? 또는 ' '인 요소 제거
    b=[]
    c=[]
    for item in a:
      count = 1
      for parts in item:
        if parts == ' ' or parts == '?':
          count = 0
      if count == 1:
        c.append(param[0].strip().lstrip('.'))
        b.append(item)
    quiz1.append(c)
    answer1.append(b)
    if(len(b) == 0):
      print(item)
    #print(c)
    #print(b)

  f.close()

  print(quiz1[0][0])
  print(answer1[1])
  #print(quiz1)
  #print(answer1)


  import mysql.connector

  # MySQL 데이터베이스 연결 설정
  mydb = mysql.connector.connect(
      host="first-my-rds.cv2cwuk4yuhc.ap-northeast-2.rds.amazonaws.com",
      user="test",
      password="myfirstrds",
      database="project"
  )

  # MySQL 데이터베이스 커서 생성
  mycursor = mydb.cursor()

  for i in range(len(quiz1)):
    for j in range(len(quiz1[i])):
      quiz = quiz1[i][j]
      answer = answer1[i][j]
      query = "INSERT INTO newquiz (quiz,answer) VALUES(%s, %s)"
      data = (quiz, answer)
      mycursor.execute(query,data)
    

  mydb.commit()
  mydb.close()

  print('끝')