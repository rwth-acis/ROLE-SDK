# SpecGen Makefile

DESTDIR=
NAME=specgen
DOCDIR=doc

install:
	mkdir -p $(DESTDIR)/usr/share/$(NAME)
	cp $(NAME).py $(DESTDIR)/usr/share/$(NAME)/$(NAME).py
	chmod 755 $(DESTDIR)/usr/share/$(NAME)/$(NAME).py
	rm -f $(DESTDIR)/usr/bin/$(NAME)
	ln -s $(DESTDIR)/usr/share/$(NAME)/$(NAME).py $(DESTDIR)/usr/bin/$(NAME)

uninstall:
	rm -f $(DESTDIR)/usr/bin/$(NAME)
	rm -rf  $(DESTDIR)/usr/share/$(NAME)

gendoc:
	rm -rf $(DOCDIR)
	mkdir -p $(DOCDIR)
	epydoc -n $(NAME) -o $(DOCDIR) --html $(NAME).py

clean:
	rm -rf $(DOCDIR)
	rm -f `find . -name "*~"`
	rm -f `find . -name "*.pyc"`
