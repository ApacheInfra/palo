# be ������˵��
## enable load strict ����˵��

be ����������һ������ enable_load_strict �����������е��뷽ʽ������ת����������������ݣ��Ƿ��ϸ� filter��

enable load strict ����ֻ�Ե����е�����ת����Ч����������ת����˵����� enable_load_strict Ϊtrue�����������ݽ���filter��

���ڵ����ĳ�а��������任�ģ������ֵ�ͺ����Ľ��һ�£�strict ���䲻����Ӱ�졣������ strftime �� broker ϵͳ֧�ֵĺ���Ҳ�������ࣩ��

### strict ������ת����ϵ

������������Ϊ int ������
ע�������е����������ֵʱ

source data | source data example | string to int   | enable_load_strict | load_data
------------|---------------------|-----------------|--------------------|---------
��ֵ        | \N                  | N/A             | true or false      | NULL
not null    | aaa                 | NULL            | true               | filtered
not null    | aaa                 | NULL            | false              | NULL
not null    | 1                   | 1               | true or false      | correct data
