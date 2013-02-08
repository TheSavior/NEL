// File generated by hadoop record compiler. Do not edit.
package org.wikipedia.miner.db.struct;

public class DbLabelForPageList extends org.apache.hadoop.record.Record {
  private static final org.apache.hadoop.record.meta.RecordTypeInfo _rio_recTypeInfo;
  private static org.apache.hadoop.record.meta.RecordTypeInfo _rio_rtiFilter;
  private static int[] _rio_rtiFilterFields;
  static {
    _rio_recTypeInfo = new org.apache.hadoop.record.meta.RecordTypeInfo("DbLabelForPageList");
    _rio_recTypeInfo.addField("Labels", new org.apache.hadoop.record.meta.VectorTypeID(new org.apache.hadoop.record.meta.StructTypeID(org.wikipedia.miner.db.struct.DbLabelForPage.getTypeInfo())));
  }
  
  private java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage> Labels;
  public DbLabelForPageList() { }
  public DbLabelForPageList(
    final java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage> Labels) {
    this.Labels = Labels;
  }
  public static org.apache.hadoop.record.meta.RecordTypeInfo getTypeInfo() {
    return _rio_recTypeInfo;
  }
  public static void setTypeFilter(org.apache.hadoop.record.meta.RecordTypeInfo rti) {
    if (null == rti) return;
    _rio_rtiFilter = rti;
    _rio_rtiFilterFields = null;
    org.wikipedia.miner.db.struct.DbLabelForPage.setTypeFilter(rti.getNestedStructTypeInfo("DbLabelForPage"));
  }
  private static void setupRtiFields()
  {
    if (null == _rio_rtiFilter) return;
    // we may already have done this
    if (null != _rio_rtiFilterFields) return;
    int _rio_i, _rio_j;
    _rio_rtiFilterFields = new int [_rio_rtiFilter.getFieldTypeInfos().size()];
    for (_rio_i=0; _rio_i<_rio_rtiFilterFields.length; _rio_i++) {
      _rio_rtiFilterFields[_rio_i] = 0;
    }
    java.util.Iterator<org.apache.hadoop.record.meta.FieldTypeInfo> _rio_itFilter = _rio_rtiFilter.getFieldTypeInfos().iterator();
    _rio_i=0;
    while (_rio_itFilter.hasNext()) {
      org.apache.hadoop.record.meta.FieldTypeInfo _rio_tInfoFilter = _rio_itFilter.next();
      java.util.Iterator<org.apache.hadoop.record.meta.FieldTypeInfo> _rio_it = _rio_recTypeInfo.getFieldTypeInfos().iterator();
      _rio_j=1;
      while (_rio_it.hasNext()) {
        org.apache.hadoop.record.meta.FieldTypeInfo _rio_tInfo = _rio_it.next();
        if (_rio_tInfo.equals(_rio_tInfoFilter)) {
          _rio_rtiFilterFields[_rio_i] = _rio_j;
          break;
        }
        _rio_j++;
      }
      _rio_i++;
    }
  }
  public java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage> getLabels() {
    return Labels;
  }
  public void setLabels(final java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage> Labels) {
    this.Labels=Labels;
  }
  public void serialize(final org.apache.hadoop.record.RecordOutput _rio_a, final String _rio_tag)
  throws java.io.IOException {
    _rio_a.startRecord(this,_rio_tag);
    {
      _rio_a.startVector(Labels,"Labels");
      int _rio_len1 = Labels.size();
      for(int _rio_vidx1 = 0; _rio_vidx1<_rio_len1; _rio_vidx1++) {
        org.wikipedia.miner.db.struct.DbLabelForPage _rio_e1 = Labels.get(_rio_vidx1);
        _rio_e1.serialize(_rio_a,"_rio_e1");
      }
      _rio_a.endVector(Labels,"Labels");
    }
    _rio_a.endRecord(this,_rio_tag);
  }
  private void deserializeWithoutFilter(final org.apache.hadoop.record.RecordInput _rio_a, final String _rio_tag)
  throws java.io.IOException {
    _rio_a.startRecord(_rio_tag);
    {
      org.apache.hadoop.record.Index _rio_vidx1 = _rio_a.startVector("Labels");
      Labels=new java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage>();
      for (; !_rio_vidx1.done(); _rio_vidx1.incr()) {
        org.wikipedia.miner.db.struct.DbLabelForPage _rio_e1;
        _rio_e1= new org.wikipedia.miner.db.struct.DbLabelForPage();
        _rio_e1.deserialize(_rio_a,"_rio_e1");
        Labels.add(_rio_e1);
      }
      _rio_a.endVector("Labels");
    }
    _rio_a.endRecord(_rio_tag);
  }
  public void deserialize(final org.apache.hadoop.record.RecordInput _rio_a, final String _rio_tag)
  throws java.io.IOException {
    if (null == _rio_rtiFilter) {
      deserializeWithoutFilter(_rio_a, _rio_tag);
      return;
    }
    // if we're here, we need to read based on version info
    _rio_a.startRecord(_rio_tag);
    setupRtiFields();
    for (int _rio_i=0; _rio_i<_rio_rtiFilter.getFieldTypeInfos().size(); _rio_i++) {
      if (1 == _rio_rtiFilterFields[_rio_i]) {
        {
          org.apache.hadoop.record.Index _rio_vidx1 = _rio_a.startVector("Labels");
          Labels=new java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage>();
          for (; !_rio_vidx1.done(); _rio_vidx1.incr()) {
            org.wikipedia.miner.db.struct.DbLabelForPage _rio_e1;
            _rio_e1= new org.wikipedia.miner.db.struct.DbLabelForPage();
            _rio_e1.deserialize(_rio_a,"_rio_e1");
            Labels.add(_rio_e1);
          }
          _rio_a.endVector("Labels");
        }
      }
      else {
        java.util.ArrayList<org.apache.hadoop.record.meta.FieldTypeInfo> typeInfos = (java.util.ArrayList<org.apache.hadoop.record.meta.FieldTypeInfo>)(_rio_rtiFilter.getFieldTypeInfos());
        org.apache.hadoop.record.meta.Utils.skip(_rio_a, typeInfos.get(_rio_i).getFieldID(), typeInfos.get(_rio_i).getTypeID());
      }
    }
    _rio_a.endRecord(_rio_tag);
  }
  public int compareTo (final Object _rio_peer_) throws ClassCastException {
    if (!(_rio_peer_ instanceof DbLabelForPageList)) {
      throw new ClassCastException("Comparing different types of records.");
    }
    DbLabelForPageList _rio_peer = (DbLabelForPageList) _rio_peer_;
    int _rio_ret = 0;
    {
      int _rio_len11 = Labels.size();
      int _rio_len21 = _rio_peer.Labels.size();
      for(int _rio_vidx1 = 0; _rio_vidx1<_rio_len11 && _rio_vidx1<_rio_len21; _rio_vidx1++) {
        org.wikipedia.miner.db.struct.DbLabelForPage _rio_e11 = Labels.get(_rio_vidx1);
        org.wikipedia.miner.db.struct.DbLabelForPage _rio_e21 = _rio_peer.Labels.get(_rio_vidx1);
        _rio_ret = _rio_e11.compareTo(_rio_e21);
        if (_rio_ret != 0) { return _rio_ret; }
      }
      _rio_ret = (_rio_len11 - _rio_len21);
    }
    if (_rio_ret != 0) return _rio_ret;
    return _rio_ret;
  }
  public boolean equals(final Object _rio_peer_) {
    if (!(_rio_peer_ instanceof DbLabelForPageList)) {
      return false;
    }
    if (_rio_peer_ == this) {
      return true;
    }
    DbLabelForPageList _rio_peer = (DbLabelForPageList) _rio_peer_;
    boolean _rio_ret = false;
    _rio_ret = Labels.equals(_rio_peer.Labels);
    if (!_rio_ret) return _rio_ret;
    return _rio_ret;
  }
  public Object clone() throws CloneNotSupportedException {
    DbLabelForPageList _rio_other = new DbLabelForPageList();
    _rio_other.Labels = (java.util.ArrayList<org.wikipedia.miner.db.struct.DbLabelForPage>) this.Labels.clone();
    return _rio_other;
  }
  public int hashCode() {
    int _rio_result = 17;
    int _rio_ret;
    _rio_ret = Labels.hashCode();
    _rio_result = 37*_rio_result + _rio_ret;
    return _rio_result;
  }
  public static String signature() {
    return "LDbLabelForPageList([LDbLabelForPage(sllzzz)])";
  }
  public static class Comparator extends org.apache.hadoop.record.RecordComparator {
    public Comparator() {
      super(DbLabelForPageList.class);
    }
    static public int slurpRaw(byte[] b, int s, int l) {
      try {
        int os = s;
        {
          int vi1 = org.apache.hadoop.record.Utils.readVInt(b, s);
          int vz1 = org.apache.hadoop.record.Utils.getVIntSize(vi1);
          s+=vz1; l-=vz1;
          for (int vidx1 = 0; vidx1 < vi1; vidx1++){
            int r = org.wikipedia.miner.db.struct.DbLabelForPage.Comparator.slurpRaw(b,s,l);
            s+=r; l-=r;
          }
        }
        return (os - s);
      } catch(java.io.IOException e) {
        throw new RuntimeException(e);
      }
    }
    static public int compareRaw(byte[] b1, int s1, int l1,
                                   byte[] b2, int s2, int l2) {
      try {
        int os1 = s1;
        {
          int vi11 = org.apache.hadoop.record.Utils.readVInt(b1, s1);
          int vi21 = org.apache.hadoop.record.Utils.readVInt(b2, s2);
          int vz11 = org.apache.hadoop.record.Utils.getVIntSize(vi11);
          int vz21 = org.apache.hadoop.record.Utils.getVIntSize(vi21);
          s1+=vz11; s2+=vz21; l1-=vz11; l2-=vz21;
          for (int vidx1 = 0; vidx1 < vi11 && vidx1 < vi21; vidx1++){
            int r1 = org.wikipedia.miner.db.struct.DbLabelForPage.Comparator.compareRaw(b1,s1,l1,b2,s2,l2);
            if (r1 <= 0) { return r1; }
            s1+=r1; s2+=r1; l1-=r1; l2-=r1;
          }
          if (vi11 != vi21) { return (vi11<vi21)?-1:0; }
        }
        return (os1 - s1);
      } catch(java.io.IOException e) {
        throw new RuntimeException(e);
      }
    }
    public int compare(byte[] b1, int s1, int l1,
                         byte[] b2, int s2, int l2) {
      int ret = compareRaw(b1,s1,l1,b2,s2,l2);
      return (ret == -1)? -1 : ((ret==0)? 1 : 0);}
  }
  
  static {
    org.apache.hadoop.record.RecordComparator.define(DbLabelForPageList.class, new Comparator());
  }
}
