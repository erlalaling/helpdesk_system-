# helpdesk_system-
This GitHub repository will serve as the main repository for the final project for Data Structure and Algorithms. The project assigned is to create a help desk that will utilize linklist, queue, priority queue, and hash table.
----
- **Programming Language**: Java Development 
- **Environment**: NetBeans IDE 
- **User Interface**: Java Swing (GUI) Group 
- **Members**: 10 students
- **Project Type**: Fully Functional Desktop Application

**Help Desk Ticket Management System**
- Register concerns, process requests, and monitor unresolved tickets.
- Queue, Priority Queue, Hash Table


**Project (5 pers?)**
- UI prototyping????? | Gerolaga
- Front and GUI??? class | Sumabat
- data processing | Banz
- file writer class | Sachi
- Database mock data | Carmela

**Docu (5pers?)**
- Introduction | Mark John
- System documentation | ate Elly
- System Flowchart | Marcelo
- Screenshot, testing QA | Aldrine
- conclusion??? | Michaela



----
## Required Collections
- Queue, Priority Queue, Hash Table

##### Queue
- Queue stores all the tickets in the order they came in

##### Hashtable
- Hashtable stores date and a priority queue
- It stores those values because it is meant to group tickets by the time they arrived

##### PriorityQueue
- Stores Ticket objects in priority order

> Each collection serves different purposes of which complements each other. The tickets are ordered this way to maintain fairness for the low priority tickets that arrived early.

----

**Incase nagkaroon ulit ng issue sa compilation, run these one by one in the terminal:**
Remove-Item -Recurse -Force .\build\classes
New-Item -ItemType Directory .\build\classes
javac -d .\build\classes .\src\helpdesksystemui\*.java
java -cp .\build\classes helpdesksystemui.HelpDeskSystemUI 
