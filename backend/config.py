
__SEED_URL__ = 'https://www.cs.uic.edu/'

__DOMAIN_OF_INTEREST__ = 'uic.edu'

__DEPARTMENT_MAPPING__ = getDepartmentMapping()



def getDepartmentMapping():
	m = dict()
	m['accc'] = 'accc'
	m['library'] = 'library'
	m['ois'] = 'ois'

	return m