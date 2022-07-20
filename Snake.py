import pygame as py
import random

display = py.display.set_mode(size=(500, 500))


def background():
    counter = 0
    while counter < 500:
        py.draw.aaline(display, (0, 0, 255), (counter, 0), (counter, 500))
        py.draw.aaline(display, (0, 0, 255), (0, counter), (500, counter))
        counter += 25


class snake:
    rect_list = []
    whereturn = []

    def __init__(self, count):
        counter = 0
        units = 25
        if count != 0:
            while counter < count:
                rect = py.Rect(counter * units, 0, 25, 25)
                py.draw.rect(display, (0, 0, 255), rect)
                self.rect_list.append([rect, 2])
                counter += 1
            py.display.update()

    def move(self):
        display.fill((0, 0, 0))
        background()
        print(self.rect_list)
        for cube in self.rect_list:
            pos = [cube[0][0], cube[0][1]]
            for coord in self.whereturn:
                if pos[0] == coord[0] and pos[1] == coord[1]:
                    print("match")
                    cube[1] = coord[2]
        for coord in self.whereturn:
            if self.rect_list[0][0][0] == coord[0] and self.rect_list[0][0][1] == coord[1]:
                self.whereturn.remove(coord)
                print("remove")

        for cube in self.rect_list:
            if cube[1] == 2:
                cube[0][0] += 25
                py.draw.rect(display, (0, 0, 255), cube[0])
            if cube[1] == 3:
                cube[0][1] += 25
                py.draw.rect(display, (0, 0, 255), cube[0])
            if cube[1] == 0:
                cube[0][0] -= 25
                py.draw.rect(display, (0, 0, 255), cube[0])
            if cube[1] == 1:
                cube[0][1] -= 25
                py.draw.rect(display, (0, 0, 255), cube[0])
        py.display.update()

    def fruit_detector(self, fcoord):
        head = self.rect_list[len(self.rect_list) - 1]
        if head[0][0] / 25 == fcoord[0] and head[0][1] / 25 == fcoord[1]:
            self.extend_body()
            return True
        return False

    def extend_body(self):
        display.fill((0, 0, 0))
        background()
        get_tail_cube = self.rect_list[0]
        if get_tail_cube[1] == 0:
            new_rect = py.Rect(get_tail_cube[0][0] + 25, get_tail_cube[0][1], 25, 25)
            self.rect_list.insert(0, [new_rect, 0])
        elif get_tail_cube[1] == 1:
            new_rect = py.Rect(get_tail_cube[0][0], get_tail_cube[0][1] + 25, 25, 25)
            self.rect_list.insert(0, [new_rect, 1])
        elif get_tail_cube[1] == 2:
            new_rect = py.Rect(get_tail_cube[0][0] - 25, get_tail_cube[0][1], 25, 25)
            self.rect_list.insert(0, [new_rect, 2])
        elif get_tail_cube[1] == 3:
            new_rect = py.Rect(get_tail_cube[0][0], get_tail_cube[0][1] - 25, 25, 25)
            self.rect_list.insert(0, [new_rect, 3])
        for cube in self.rect_list:
            py.draw.rect(display, (255, 255, 255), cube[0])
        py.display.update()

    def get_fruit(self):
        ran = True
        counter = 0
        while ran:
            list_counter = 0
            x = random.randint(0, (500 / 25) - 1)
            y = random.randint(0, (500 / 25) - 1)
            counter += 1
            for cube in self.rect_list:
                list_counter += 1
                if cube[0][0] / 25 == x and cube[0][1] / 25 == y:
                    break
                if list_counter == len(self.rect_list):
                    ran = False

        print("counter" + str(counter))
        coord = (x, y)
        return coord

    def collision_detector(self):
        for cube in self.rect_list:
            if cube != self.rect_list[len(self.rect_list) - 1]:
                if cube[0][0] == self.rect_list[len(self.rect_list) - 1][0][0] and \
                        cube[0][1] == self.rect_list[len(self.rect_list) - 1][0][1]:
                    return True


class main:
    background()
    sk = snake(3)
    eventid = py.USEREVENT
    list_update = py.USEREVENT + 1
    py.time.set_timer(eventid, 200)
    py.time.set_timer(list_update, 10)

    coord = sk.get_fruit()

    on = True
    while on:
        for e in py.event.get():
            if e.type == py.KEYDOWN:
                key = py.key.get_pressed()
                if key[py.K_DOWN]:
                    head = sk.rect_list[len(sk.rect_list) - 1]
                    sk.whereturn.append([head[0][0], head[0][1], 3])
                if key[py.K_UP]:
                    head = sk.rect_list[len(sk.rect_list) - 1]
                    sk.whereturn.append([head[0][0], head[0][1], 1])
                if key[py.K_LEFT]:
                    head = sk.rect_list[len(sk.rect_list) - 1]
                    sk.whereturn.append([head[0][0], head[0][1], 0])
                if key[py.K_RIGHT]:
                    head = sk.rect_list[len(sk.rect_list) - 1]
                    sk.whereturn.append([head[0][0], head[0][1], 2])
            if e.type == eventid:
                sk.move()
                is_detect = sk.fruit_detector(coord)
                if is_detect:
                    coord = sk.get_fruit()
                    is_detect = False
                py.draw.rect(display, (255, 0, 0), (coord[0] * 25, coord[1] * 25, 25, 25))
                py.display.update()
                got_detect = sk.collision_detector()
                if got_detect:
                    on = False
            if e.type == py.QUIT:
                on = False


main()
